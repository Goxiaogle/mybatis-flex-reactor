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
class UserService : ReactorServiceImpl<UserMapper, User>()
``` 
仅需两步，你就已经得到了一个具备响应式的 Service

## 有什么用？
当你开始使用响应式数据库操作后，就再也无需再担心内存占用问题，与内存爆满说拜拜

### 与 EasyExcel 交互示例：巨量数据从数据库写入 Excel
```kotlin
class UserService : ReactorServiceImpl<UserMapper, User>() {
    // 在 WebFlux 中，可以将此方法直接作为 Controller 的返回值，供 WebFlux 进行调度，返回数据给前端
    fun writeAllToExcel(): Mono<String> {
        // 目标文件
        val tempFile = File.createTempFile("user", ".xlsx")
        // 创建 EasyExcel 的 writer 与目标 sheet
        val writer = EasyExcel.write(tempFile, UserExportVo::class.java)
            .registerWriteHandler(LongestMatchColumnWidthStyleStrategy())
            .build()
        val sheet = EasyExcel.writerSheet(1).build()
        // 调用响应式的 listAs 方法
        return listAs(QueryWrapper.create(), UserExportVo::class.java)
            // 这里可以预处理数据
            .doOnNext { it.name = "${it.id} ${it.name}" }
            // 选择缓存大小，即多少条数据一起写入 Excel
            .buffer(100)
            // 写入到 Excel
            .doOnNext { writer.write(it, sheet) }
            // 做结尾操作，使用 then 只接收信号而不接收数据
            .then()
            // 结束后关闭 writer
            .doOnTerminate { writer.finish() }
            // 返回文件路径
            .thRerturn(tempFile.absolutePath)
            // 即使出错也会关闭 writer，防止可能的资源泄漏
            .doOnError { 
                writer.finish()
                it.printStackTrace()
            }
    }
}
```

### 与前端 fetch 交互示例：对于大量数据分页的替代，数据即时显示，无需等到全部数据加载完
同时可以配合前端各种框架的“虚拟滚动”或者“虚拟表格”组件，减少内存占用，提升性能。下面为原生 JavaScript 的示例：

后端：
```kotlin
@Service
class UserService : ReactorServiceImpl<UserMapper, User>()

@RestController
class UserController(
    private val userService: UserService
) {
    /**
     * ND_JSON_VALUE 可以让 Flux 每条数据都是单独的 JSON 格式
     * 返回数据类似：
     * > {...}
     * > {...}
     * > {...}
     * 若未设定 produces，会是：
     * > [{...},
     * > {...},
     * > {...}]
     */
    @GetMapping("/users", produces = [MediaType.APPLICATION_NDJSON_VALUE])
    fun getUsers(): Flux<User> = userService.list()
        // 每条数据模拟延迟 1 秒
        .delayElements(Duration.ofSeconds(1))
}
```

前端：
```html
<!DOCTYPE html>
<html lang="cn">
    <head>
        <meta charset="UTF-8">
        <title>Reactor - Test</title>
    </head>
    <body>
        <div id="table" style="display:flex;flex-direction: column">

        </div>

        <script>
            const element = document.querySelector('#table');
            const decoder = new TextDecoder();
            const loadData = async () => {
                const response = await fetch('http://localhost:8080/users');
                const reader = response.body.getReader();
                // 不断读取数据直到收到完成的指令
                while (true) {
                    const {done, value} = await reader.read();
                    if (done) {
                        break;
                    }
                    const text = decoder.decode(value).trim();
                    // WebFlux 返回数据可能会一次返回几行单个数据，所以这里需要手动分割
                    text.split('\n').forEach(line => {
                        // 解析成 JSON
                        line = JSON.parse(line);
                        element.innerHTML += `<span>${line.id} --- ${line.name.substring(0, 1)}</span>`;
                    });
                }
            };
            loadData();
        </script>
    </body>
</html>
```
![fetch-demo](images/fetch-demo.gif)

### 异步写入日志
很多时候日志的优先级并没有那么高，但总是需要进行记录，有时候日志数据量比较大（或者需要进行额外的网络请求），就会堵塞主逻辑较长时间。
此时我们就可以采用 ReactorService 来解决：

```kotlin
@Service
class LogService : ReactorServiceImpl<LogMapper, Log>()

class TestController(
    private val logService: LogService
) {
    @GetMapping("/test")
    fun test() { 
        println("1")
        // 2
        logService.save(Log("test"))
            // 将执行时机改为弹性调度，避免阻塞主线程
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()
        /**
         * 你也可以用下面这个方式：
         * ReactorUtils.runAsync(
         *     logService.save(Log("test"))
         * )
         * 与上一种方式等效
         */
        println("3")
        
        // 观察控制台输出，结果将会是 1 -> 3 -> 2(SQL 日志)
    }
}
```