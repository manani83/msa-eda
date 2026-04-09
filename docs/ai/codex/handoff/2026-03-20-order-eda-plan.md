# Order EDA Handoff

## 목적
- 현재 동기식 주문 생성 구조를 이해하고, 이후 Kafka 기반 EDA 구조로 전환 작업을 이어서 진행하기 위한 핸드오프 문서.

## 현재 상태
- 주문 생성 진입점은 `OrderController -> CreateOrderBizImpl -> CreateOrderService` 순서로 변경되었다.
- 트랜잭션 경계는 `CreateOrderBizImpl#create`에 있다.
- 주문은 혜택 요청이 있으면 `PENDING_BENEFITS`, 혜택 요청이 없으면 같은 트랜잭션 안에서 `BENEFITS_COMPLETED`로 마무리된다.
- 주문 저장과 함께 `order_benefit_saga` 테이블에 Saga 상태가 저장된다.
- 중앙 orchestrator가 첫 command를 결정한다.
  - 쿠폰 요청이 있으면 `coupon.apply.command.v1`
  - 포인트만 요청되면 `point.reserve.command.v1`
- 주문번호는 여전히 저장 시점에 `OrderPersistenceAdapter`에서 생성된다.
- outbox publisher가 `PENDING` 또는 `FAILED` row를 조회하고 Kafka 발행 후 `PUBLISHED`/`FAILED`로 갱신할 수 있다.
- scheduler는 `order.outbox.publisher.enabled=true` 일 때만 활성화된다.
- coupon/point 결과를 받는 order 쪽 listener와 `OrderSagaOrchestrator`가 추가되었다.
- coupon 모듈에는 `coupon.apply.command.v1` consumer와 `coupon.apply.result.v1` publisher가 추가되었다.
- point 서비스 측 command consumer/result publisher는 아직 없다.

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

## 2026-03-31 추가 반영
- outbox publisher 구현
  - `spring-kafka` 의존성 추가
  - `OutboxQueryPort`, `OutboxRecord` 추가
  - `OutboxPersistenceAdapter`가 publishable 조회와 `markPublished` / `markFailed`를 처리하도록 확장
  - `OrderOutboxPublisher` 추가
  - `OrderOutboxPublisherScheduler`와 scheduling configuration 추가
- 발행 흐름
  - `PENDING`, `FAILED` 상태 row 조회
  - Kafka 발행 성공 시 `PUBLISHED`와 `publishedAt` 갱신
  - Kafka 발행 실패 시 `FAILED`, `retryCount`, `nextAttemptAt`, `lastErrorMessage` 갱신
- 테스트 변경
  - publisher 성공/실패 전이를 검증하는 테스트 추가
  - `./gradlew :order:test` 통과

## 2026-04-02 추가 반영
- orchestration 기반 Saga 구조 도입
  - `CreateOrderBizImpl`이 직접 `order.created`를 적재하던 구조에서 `OrderSagaOrchestrator` 중심 구조로 변경
  - `OrderBenefitSaga`, `OrderSagaStep`, `CouponProcessStatus`, `PointProcessStatus` 추가
  - `order_benefit_saga` 저장소 추가
- command/result 이벤트 구조 전환
  - `CouponApplyCommandEvent`, `PointReserveCommandEvent` 추가
  - topic을 `coupon.apply.command.v1`, `coupon.apply.result.v1`, `point.reserve.command.v1`, `point.reserve.result.v1`로 전환
  - `CreateOrderRequest`, `CreateOrderCommand`에 `pointAmount` 추가
- 중앙 결과 처리
  - `OrderSagaOrchestrator`가 coupon/point 결과를 받아 다음 command 적재 또는 주문 최종 상태 갱신 수행
  - `CouponResultKafkaListener`, `PointResultKafkaListener` 추가
- 테스트 변경
  - 주문 생성 통합 테스트를 command + saga 기준으로 갱신
  - orchestrator 통합 테스트 추가
  - `./gradlew :order:test` 통과

## 2026-04-03 추가 반영
- coupon 서비스 측 command/result 경로 구현
  - `CouponApplyCommandKafkaListener` 추가
  - `CouponApplyCommandBiz`, `CouponApplyCommandBizImpl` 추가
  - coupon 전용 message envelope/payload/result code/failure reason 추가
  - `CouponResultKafkaPublisher` 추가
  - `coupon.command.listener.enabled=true`일 때만 listener 활성화
- 테스트 변경
  - Biz 단위 테스트 추가
  - Kafka listener/publisher 단위 테스트 추가
  - `./gradlew test` 통과

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
  | body: userId, items, shippingAddress, couponCode, pointAmount
  v
[OrderController]
  | 1. 요청 검증
  | 2. request -> CreateOrderCommand
  v
[CreateOrderBizImpl]  @Transactional
  | 3. 주문 생성 기준 시각 생성
  | 4. CreateOrderService.createPendingOrder(...)
  | 5. Order 저장
  | 6. OrderSagaOrchestrator.start(...)
  v
[OrderSagaOrchestrator]
  | 7. saga 저장
  | 8. 첫 command 선택
  |    - coupon.apply.command
  |    - point.reserve.command
  |    - 혜택 없으면 주문 완료
  v
[DB Commit]
  |
  +--> [orders table]
  |
  +--> [order_outbox table]
  |
  +--> [order_benefit_saga table]
  v
[Client]
  | 200 OK
  | orderId, status=PENDING_BENEFITS or BENEFITS_COMPLETED
```

## 현재 구조 + orchestration 흐름도

```text
[CreateOrderBizImpl]  @Transactional
  | 1. 주문 저장
  | 2. saga 저장
  | 3. 첫 command outbox 적재
  v
[DB Commit]
  |
  +--> [orders table]
  |
  +--> [order_benefit_saga table]
  |
  +--> [order_outbox table(status=PENDING)]

[OrderOutboxPublisherScheduler]
  | 4. enabled=true 일 때 fixed-delay polling
  v
[OrderOutboxPublisher]
  | 5. publishable outbox 조회(PENDING, FAILED)
  | 6. KafkaTemplate으로 payload_json 발행
  | 7. 성공 시 PUBLISHED / publishedAt 갱신
  | 8. 실패 시 FAILED / retryCount / nextAttemptAt 갱신
  v
[Kafka Topic: coupon.apply.command.v1 / point.reserve.command.v1]

[External Coupon/Point Service]
  | 9. command 처리
  | 10. result 발행
  v
[Kafka Topic: coupon.apply.result.v1 / point.reserve.result.v1]

[CouponResultKafkaListener / PointResultKafkaListener]
  | 11. 결과 수신
  v
[OrderSagaOrchestrator]
  | 12. saga 상태 갱신
  | 13. 다음 command 적재 또는 주문 상태 최종 반영
```

## 합의된 EDA 전환 방향
- Controller에 오케스트레이션을 두지 않는다.
- 주문 생성 시작점과 후속 혜택 제어는 `CreateOrderBiz`와 `OrderSagaOrchestrator` 같은 유스케이스 계층으로 둔다.
- 주문 생성과 이벤트 발행은 `transactional outbox` 패턴으로 처리한다.
- coupon/point는 command를 수행하고 result를 돌려주는 worker 역할만 가진다.
- 다음 단계 결정은 order 쪽 orchestrator가 한다.

## 목표 오케스트레이션 흐름도

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
  | 5. orders 저장
  | 6. saga 저장
  | 7. 첫 command outbox 적재
  v
[DB Commit]
  |
  +--> [orders table]
  |
  +--> [order_benefit_saga table]
  |
  +--> [order_outbox table]

[OutboxPublisher]
  | 8. outbox polling or CDC
  | 9. Kafka로 command 발행
  v
[Kafka Topic: coupon.apply.command / point.reserve.command]

[CouponConsumer / PointConsumer]
  | 10. command 처리
  | 11. result 이벤트 발행
  v
[Kafka Topic: coupon.apply.result / point.reserve.result]

[OrderSagaOrchestrator]
  | 12. result 수신
  | 13. saga 상태 갱신
  | 14. 다음 command 적재 또는 주문 최종 반영
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
1. coupon 서비스 쪽 `coupon.apply.command` consumer와 `coupon.apply.result` publisher 구현
2. point 서비스 쪽 `point.reserve.command` consumer와 `point.reserve.result` publisher 구현
3. app/runtime Kafka 설정 추가
4. 보상 정책과 실패 재처리 정책 구체화
5. 응답 코드를 `202 Accepted`로 바꿀지 검토
6. 멀티 인스턴스 환경이 필요하면 outbox claim/lock 전략 검토

## 다음 세션에서 바로 사용할 요청 예시
- `docs/ai/codex/handoff/2026-03-20-order-eda-plan.md 기준으로 다음 단계 진행해줘`
- `handoff 문서 기준으로 coupon consumer부터 구현해줘`
- `2026-03-20-order-eda-plan.md 읽고 주문 결과 집계 핸들러부터 붙여줘`
- `현재 handoff 문서 기준으로 EDA 전환 작업 이어서 진행해줘`

## 참고 문서
- `docs/ai/codex/2026/03/2026-03-20.md`
- `docs/team/TEAM_CONVENTION.md`
- `docs/team/REFACTORING_PLAYBOOK.md`
