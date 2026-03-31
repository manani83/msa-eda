# Order EDA Handoff

## 목적
- 현재 동기식 주문 생성 구조를 이해하고, 이후 Kafka 기반 EDA 구조로 전환 작업을 이어서 진행하기 위한 핸드오프 문서.

## 현재 상태
- 주문 생성 진입점은 `OrderController -> CreateOrderBizImpl -> CreateOrderService` 순서로 변경되었다.
- 트랜잭션 경계는 `CreateOrderBizImpl#create`에 있다.
- 주문은 이제 쿠폰을 동기 검증하지 않고 `PENDING_BENEFITS` 상태로 먼저 저장된다.
- 주문 저장과 함께 `order_outbox` 테이블에 `order.created` 이벤트가 같은 트랜잭션으로 적재된다.
- 주문번호는 여전히 저장 시점에 `OrderPersistenceAdapter`에서 생성된다.
- Kafka publisher / consumer는 아직 붙지 않았다.
- 쿠폰/적립금 최종 반영 로직도 아직 없다.

## 오늘 반영된 코드 변경
- 입력 검증 추가
  - `CreateOrderRequest`에 Bean Validation 추가
  - `OrderController`에 `@Valid` 적용
  - 잘못된 요청에 대한 `400` 테스트 추가
- 트랜잭션 경계 이동
  - `CreateOrderService#create`에 `@Transactional` 적용
  - `OrderPersistenceAdapter#save`의 `@Transactional` 제거
- 시간 일관성 정리
  - 도메인 `createdAt`을 엔티티에 직접 저장하도록 변경
  - 주문번호 생성 기준 시각과 저장 시각 일치
- 매핑 중복 축소
  - `CreateOrderRequest#toCommand()` 추가
  - `CreateOrderCommand#toOrder(...)` 추가
  - 컨트롤러/서비스의 수작업 매핑 제거

## 2026-03-24 추가 반영
- 주문 상태 모델 재설계
  - `OrderStatus`를 `PENDING_BENEFITS`, `BENEFITS_COMPLETED`, `BENEFITS_FAILED`, `PARTIAL_FAILED` 중심으로 확장
  - 현재 동기 흐름의 `Order.create(...)`는 `BENEFITS_COMPLETED`로 저장되도록 조정
  - 향후 EDA 시작점에서 사용할 `Order.createPendingBenefits(...)` 추가
  - 도메인/통합 테스트로 상태 변경 방향 검증
- 이벤트 스키마 정의
  - `application.event.common`, `application.event.order`, `application.event.coupon`, `application.event.point`로 경계 분리
  - `EventEnvelope<T>` 공통 이벤트 envelope 추가
  - 주문은 `order`, 쿠폰은 `coupon`, 포인트는 `point` 패키지로 분리
  - `CouponFailureReason`, `PointFailureReason`, result code enum 추가
  - `OrderEventTopics`로 topic 이름과 message key 규칙 정리
  - 단위 테스트로 envelope/payload/topic 상수 검증

## 2026-03-26 추가 반영
- Biz 오케스트레이션 경계 도입
  - `CreateOrderBiz`, `CreateOrderBizImpl` 추가
  - Controller가 Biz를 호출하도록 변경
  - 트랜잭션 경계를 Biz로 이동
- pending 주문 생성 경로 전환
  - `CreateOrderService`는 `createPendingOrder(...)`만 담당
  - `CreateOrderCommand`는 `toPendingOrder(...)`로 변경
  - 주문 생성 응답 상태는 `PENDING_BENEFITS`
  - 할인 금액은 `0`, 총액은 subtotal 기준으로 반환
- transactional outbox 기초 구현
  - `OutboxCommandPort`, `OutboxMessage`, `OutboxStatus` 추가
  - `OrderOutboxEntity`, `OrderOutboxJpaRepository`, `OutboxPersistenceAdapter` 추가
  - `order.created` 이벤트를 `order_outbox`에 JSON payload로 저장
  - `payload_json`은 `LONGTEXT`로 저장되도록 설정
- 테스트 변경
  - 컨트롤러 테스트를 `PENDING_BENEFITS` 응답 기준으로 수정
  - 통합 테스트에서 주문과 outbox가 같은 트랜잭션으로 저장/롤백되는지 검증
  - 쿠폰 코드는 동기 검증 없이 접수되는 흐름으로 변경

## EDA 전환 전 과거 흐름도

```text
[Client]
  |
  | POST /orders
  | body: userId, items, shippingAddress, couponCode
  v
[OrderController]
  | 1. 요청 검증
  | 2. request -> CreateOrderCommand
  v
[CreateOrderService]  @Transactional
  | 3. 주문 생성 기준 시각 생성
  | 4. couponCode 존재 시 쿠폰 조회
  v
[CouponQueryPort]
  v
[CouponPersistenceAdapter]
  v
[CouponJpaRepository / coupons table]

[CreateOrderService]
  | 5. command -> Order 변환
  | 6. 주문 합계 계산
  | 7. 쿠폰 할인 계산
  | 8. discountAmount / totalAmount 확정
  | 9. Order 저장 요청
  v
[OrderCommandPort]
  v
[OrderPersistenceAdapter]
  | 10. 주문번호 생성
  | 11. OrderEntity 변환
  | 12. orders / order_items 저장
  v
[OrderJpaRepository]
  v
[DB]

[OrderPersistenceAdapter]
  | 13. 저장 결과를 Order로 재구성
  v
[CreateOrderService]
  | 14. CreateOrderResult 생성
  v
[OrderController]
  | 15. CreateOrderResponse 생성
  v
[Client]
  | 200 OK
  | orderId, status, couponCode, discountAmount, totalAmount, createdAt
```

## 현재 구조 기준 흐름도

```text
[Client]
  |
  | POST /orders
  | body: userId, items, shippingAddress, couponCode
  v
[OrderController]
  | 1. 요청 검증
  | 2. request -> CreateOrderCommand
  v
[CreateOrderBizImpl]  @Transactional
  | 3. 주문 생성 기준 시각 생성
  | 4. CreateOrderService.createPendingOrder(...)
  v
[CreateOrderService]
  | 5. command -> pending Order 변환
  | 6. status = PENDING_BENEFITS
  | 7. discountAmount = 0
  | 8. totalAmount = subtotal
  v
[OrderCommandPort]
  v
[OrderPersistenceAdapter]
  | 9. 주문번호 생성
  | 10. orders / order_items 저장
  v
[OutboxCommandPort]
  v
[OutboxPersistenceAdapter]
  | 11. order.created 이벤트를 order_outbox 저장
  v
[DB Commit]
  |
  +--> [orders table]
  |
  +--> [order_outbox table]
  v
[Client]
  | 200 OK
  | orderId, status=PENDING_BENEFITS, couponCode, discountAmount=0, totalAmount=subtotal
```

## 합의된 EDA 전환 방향
- Controller에 오케스트레이션을 두지 않는다.
- 주문 생성 시작점은 `CreateOrderBiz` 또는 `OrderOrchestrationService` 같은 유스케이스 계층으로 둔다.
- 주문 생성과 이벤트 발행은 `transactional outbox` 패턴으로 처리한다.
- 쿠폰/적립금은 Kafka consumer로 분리한다.
- 주문은 먼저 생성하고 `PENDING_BENEFITS` 상태로 응답한다.
- 이후 쿠폰/적립금 결과 이벤트를 수신해 주문 상태와 금액을 갱신한다.

## EDA 전환 후 목표 흐름도

```text
[Client]
  |
  | POST /orders
  | body: userId, items, shippingAddress, couponCode, pointAmount
  v
[OrderController]
  | 1. 요청 검증
  | 2. request -> CreateOrderCommand
  v
[CreateOrderBiz / OrderOrchestrationService]  @Transactional
  | 3. 주문번호 생성
  | 4. 초기 주문 생성
  |    status = PENDING_BENEFITS
  | 5. orders 저장
  | 6. outbox에 OrderCreated 이벤트 저장
  v
[DB Commit]
  |
  +--> [orders table]
  |
  +--> [order_outbox table]

[OutboxPublisher]
  | 7. outbox polling or CDC
  | 8. Kafka로 OrderCreated 발행
  v
[Kafka Topic: order.created]

  +--------------------------------------------------+
  |                                                  |
  v                                                  v

[CouponConsumer]                               [PointConsumer]
  | 9. 쿠폰 검증/적용                            | 9. 적립금 차감/예약
  | 10. 결과 이벤트 발행                         | 10. 결과 이벤트 발행
  v                                              v
[Kafka Topic: coupon.result]                   [Kafka Topic: point.result]

                  \                            /
                   \                          /
                    v                        v

            [OrderBenefitResultHandler / OrderSagaHandler]
              | 11. coupon 결과 수신
              | 12. point 결과 수신
              | 13. 두 결과 집계
              | 14. 주문 상태/금액 최종 반영
              |     - BENEFITS_COMPLETED
              |     - BENEFITS_FAILED
              |     - PARTIAL_FAILED
              v
            [orders table update]

[Client]
  | 즉시 응답:
  | 202 Accepted or 200 OK
  | orderId, status=PENDING_BENEFITS
  |
  | 이후 조회 API / 이벤트 / 폴링으로 최종 상태 확인
```

## 추천 패키지 구조

```text
order
└─ src/main/java/com/example/hexagonal
   ├─ adapters
   │  └─ order
   │     ├─ in
   │     │  ├─ web
   │     │  └─ message
   │     └─ out
   │        ├─ persistence
   │        └─ message
   ├─ application
   │  ├─ event
   │  │  ├─ common
   │  │  ├─ order
   │  │  ├─ coupon
   │  │  └─ point
   │  └─ order
   │     ├─ port
   │     │  ├─ in
   │     │  └─ out
   │     ├─ CreateOrderBiz
   │     ├─ CreateOrderService
   │     ├─ OrderBenefitSagaHandler
   │     └─ OrderBenefitResultService
   └─ domain
      └─ order
```

## 미결정 사항
- 주문 생성 직후 응답을 `202 Accepted`로 할지 `200 OK`로 할지
- 주문 상태값을 어디까지 세분화할지
- 쿠폰/적립금 실패 시 부분 성공 허용 여부
- outbox 발행 방식을 polling으로 할지 CDC로 할지

## 다음에 바로 진행할 추천 순서
1. Kafka publisher 추가
   - `order_outbox`에서 `PENDING` row 조회
   - Kafka 발행 성공 시 `PUBLISHED`, 실패 시 retry 정보 갱신
2. Coupon/Point consumer 추가
3. 주문 결과 집계 핸들러 추가
4. 주문 최종 상태/금액 갱신 로직 추가
5. 응답 코드를 `202 Accepted`로 바꿀지 검토
6. 필요 시 outbox polling을 CDC로 전환 검토

## 다음 세션에서 바로 사용할 요청 예시
- `docs/ai/codex/handoff/2026-03-20-order-eda-plan.md 기준으로 다음 단계 진행해줘`
- `handoff 문서 기준으로 Kafka publisher부터 구현해줘`
- `2026-03-20-order-eda-plan.md 읽고 outbox publisher부터 붙여줘`
- `현재 handoff 문서 기준으로 EDA 전환 작업 이어서 진행해줘`

## 참고 문서
- `docs/ai/codex/2026/03/2026-03-20.md`
- `docs/team/TEAM_CONVENTION.md`
- `docs/team/REFACTORING_PLAYBOOK.md`
