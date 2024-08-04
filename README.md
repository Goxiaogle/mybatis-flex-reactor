# Mybatis Flex Reactor

此项目基于 Mybatis Flex 构建，仅作拓展，不更改原有逻辑。封装了 Cursor 操作，让 Mybatis Flex 支持**响应式**。

无需破坏原有项目代码，也可以在非 WebFlux 项目中使用
- 如配合 easy-excel-reactor 实现响应式 EXCEL 操作，无需再担心内存占用问题
- 再比如需要做一些异步非堵塞的数据库操作（写入日志）

## 使用方法
下面假定在 Spring Boot 项目中使用 Mybatis Flex
1. 引入依赖
```kotlin
// mybatis-flex
implementation("com.mybatis-flex:mybatis-flex-spring-boot3-starter:1.9.2")
// mybatis-flex-reactor 拓展
implementation("com.juxest:mybatis-flex-reactor-spring:0.1")
```
2. 继承 ServiceReactorImpl，实现 ReactorService 接口
```kotlin
class UserService : ReactorServiceImpl<UserMapper, User>(), ReactorService<User> {
    
}
``` 