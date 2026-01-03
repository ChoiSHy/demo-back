# Demo Common Module

공통 기능을 제공하는 모듈입니다.

## 제공 기능

### 1. Base Entity 클래스

#### BaseTimeEntity
생성일시와 수정일시만 필요한 엔티티에 사용합니다.

```java
@Entity
@Table(name = "simple_entities")
public class SimpleEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
}
```

**포함 필드:**
- `createdAt`: 생성일시 (자동 설정)
- `updatedAt`: 수정일시 (자동 설정)

#### BaseEntity
전체 감사(Audit) 정보와 논리 삭제(Soft Delete) 기능이 필요한 엔티티에 사용합니다.

```java
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String name;
}
```

**포함 필드:**
- `createdAt`: 생성일시 (자동 설정)
- `updatedAt`: 수정일시 (자동 설정)
- `createdBy`: 생성자 (자동 설정, 현재 인증된 사용자)
- `updatedBy`: 수정자 (자동 설정, 현재 인증된 사용자)
- `deleted`: 삭제 여부 (기본값: false)
- `deletedAt`: 삭제일시

**제공 메서드:**
- `delete()`: 논리 삭제 (deleted = true, deletedAt 설정)
- `restore()`: 삭제 복구 (deleted = false, deletedAt 제거)

### 2. JPA Auditing 설정

`JpaAuditingConfig`가 자동으로 다음 기능을 제공합니다:

- **@CreatedDate, @LastModifiedDate**: 자동으로 생성/수정 일시 설정
- **@CreatedBy, @LastModifiedBy**: 현재 인증된 사용자 정보 자동 설정
  - 인증되지 않은 경우: "system"
  - 익명 사용자: "anonymous"
  - 인증된 사용자: 사용자 이메일 또는 username

### 3. 공통 DTO

#### BaseResponse<T>
통일된 API 응답 형식을 제공합니다.

```java
// 성공 응답
BaseResponse<UserDto> response = BaseResponse.success(userDto);
BaseResponse<UserDto> response = BaseResponse.success("회원가입 완료", userDto);

// 에러 응답
BaseResponse<Void> response = BaseResponse.error("잘못된 요청입니다");
```

**응답 형식:**
```json
{
  "success": true,
  "message": "Success",
  "data": { ... }
}
```

### 4. 예외 처리

#### BusinessException
비즈니스 로직 예외를 처리합니다.

```java
if (!userRepository.existsByEmail(email)) {
    throw new BusinessException(ErrorCode.ENTITY_NOT_FOUND);
}
```

#### ErrorCode
표준화된 에러 코드를 제공합니다.

```java
public enum ErrorCode {
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "Invalid input value"),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "Entity not found"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A001", "Invalid credentials"),
    // ...
}
```

#### GlobalExceptionHandler
전역 예외를 자동으로 처리하고 통일된 형식으로 응답합니다.

- `BusinessException`: 비즈니스 예외
- `MethodArgumentNotValidException`: Validation 실패
- `BindException`: 바인딩 실패
- `Exception`: 기타 예외

## 사용 예제

### 엔티티 정의

```java
@Entity
@Table(name = "posts")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
```

### 논리 삭제 사용

```java
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    @Transactional
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        // 논리 삭제
        post.delete();
        // deleted = true, deletedAt = 현재시간으로 자동 설정
    }

    @Transactional
    public void restorePost(Long id) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.ENTITY_NOT_FOUND));

        // 삭제 복구
        post.restore();
        // deleted = false, deletedAt = null로 자동 설정
    }
}
```

### 논리 삭제된 데이터 제외하기

```java
public interface PostRepository extends JpaRepository<Post, Long> {

    // 삭제되지 않은 데이터만 조회
    @Query("SELECT p FROM Post p WHERE p.deleted = false")
    List<Post> findAllNotDeleted();

    // 삭제되지 않은 데이터 중 특정 조건 조회
    @Query("SELECT p FROM Post p WHERE p.deleted = false AND p.title LIKE %:keyword%")
    List<Post> searchByTitle(@Param("keyword") String keyword);
}
```

### Controller에서 사용

```java
@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<PostDto>> getPost(@PathVariable Long id) {
        PostDto post = postService.getPost(id);
        return ResponseEntity.ok(BaseResponse.success(post));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<PostDto>> createPost(@Valid @RequestBody CreatePostRequest request) {
        PostDto post = postService.createPost(request);
        return ResponseEntity.ok(BaseResponse.success("게시글이 생성되었습니다", post));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.ok(BaseResponse.success("게시글이 삭제되었습니다", null));
    }
}
```

## 주의사항

1. **JPA Auditing 활성화**: `@EnableJpaAuditing`은 common 모듈에 이미 설정되어 있으므로, 다른 모듈에서 중복 설정하지 마세요.

2. **ComponentScan 설정**: Spring Boot 애플리케이션 클래스에서 common 패키지를 스캔하도록 설정하세요.
   ```java
   @SpringBootApplication
   @ComponentScan(basePackages = {"com.example.your-module", "com.example.common"})
   public class YourApplication {
       // ...
   }
   ```

3. **논리 삭제**: 기본적으로 모든 조회 쿼리에서 `deleted = false` 조건을 추가해야 합니다. 또는 `@Where` 애노테이션을 사용할 수 있습니다.
   ```java
   @Entity
   @Where(clause = "deleted = false")
   public class Post extends BaseEntity {
       // ...
   }
   ```

4. **CreatedBy/UpdatedBy**: Security Context에서 현재 사용자 정보를 가져오므로, Spring Security가 설정되어 있어야 합니다.
