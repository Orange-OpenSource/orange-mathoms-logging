# Details about Error Hash

This page gives details about the Error Hash feature (goal and implementation).

---

## Why generating errors hash?

Actually the `stack_hash` is meant to identify an error (throwable) with a **short** and **stable** signature, that 
will help matching several distinct occurrences of the same kind of error:

* **short** for easing Elasticsearch indexing, and take advantage of it (that's why we use a hex encoded hash),
* **stable** is the tricky part, as the same kind of error occurring twice may not generate exactly the same stack trace (see below).

This done, it becomes easy with Elasticsearch or any other logs centralization and indexation system to:

* **count** distinct kind of errors that occur in your code over time,
* **count** occurrences and frequency of a given kind of error,
* **detect** when a (new) kind of error occurred for the first time (maybe linking this to a new version being deployed?).

In some cases, the error hash may almost become a bug ID that you can link your bug tracker with...

---

## Example

### Let's consider error stack 1

Note: the stack trace presented here has been cut by half from useless lines

<pre>
<b>com.xyz.MyApp$MyClient$MyClientException</b>: <strike>An error occurred while getting Alice's things</strike><sup>(msg)</sup>
  at <b>com.xyz.MyApp$MyClient.getTheThings(MyApp.java:26)</b>
  at <b>com.xyz.MyApp$MyService.displayThings(MyApp.java:16)</b>
  at <strike>com.xyz.MyApp$MyService$$FastClassByCGLIB$$e7645040.invoke()</strike><sup>(aop)</sup>
  at <i>net.sf.cglib.proxy.MethodProxy.invoke()</i><sup>(aop)</sup>
  at <i>org.springframework.aop.framework.Cglib2AopProxy$CglibMethodInvocation.invokeJoinpoint()</i><sup>(fwk)</sup>
  at <i>org.springframework.aop.framework.ReflectiveMethodInvocation.proceed()</i><sup>(fwk)</sup>
  at <i>org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint.proceed()</i><sup>(fwk)</sup>
  at <i>sun.reflect.NativeMethodAccessorImpl.invoke0()</i><sup>(aop)</sup>
  at <i>sun.reflect.NativeMethodAccessorImpl.invoke()</i><sup>(aop)</sup>
  at <i>sun.reflect.DelegatingMethodAccessorImpl.invoke()</i><sup>(aop)</sup>
  at <i>java.lang.reflect.Method.invoke()</i><sup>(aop)</sup>
  at <i>org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethodWithGivenArgs()</i><sup>(fwk)</sup>
  at <i>org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethod()</i><sup>(fwk)</sup>
  at <i>org.springframework.aop.aspectj.AspectJAroundAdvice.invoke()</i><sup>(fwk)</sup>
  at <i>org.springframework.aop.framework.ReflectiveMethodInvocation.proceed()</i><sup>(fwk)</sup>
  at <i>org.springframework.aop.interceptor.AbstractTraceInterceptor.invoke()</i><sup>(fwk)</sup>
  at <i>org.springframework.aop.framework.ReflectiveMethodInvocation.proceed()</i><sup>(fwk)</sup>
  at <i>org.springframework.transaction.interceptor.TransactionInterceptor.invoke()</i><sup>(fwk)</sup>
  at <i>org.springframework.aop.framework.ReflectiveMethodInvocation.proceed()</i><sup>(fwk)</sup>
  at <i>org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke()</i><sup>(fwk)</sup>
  at <i>org.springframework.aop.framework.ReflectiveMethodInvocation.proceed()</i><sup>(fwk)</sup>
  at <i>org.springframework.aop.framework.Cglib2AopProxy$DynamicAdvisedInterceptor.intercept()</i><sup>(fwk)</sup>
  at <strike>com.xyz.MyApp$MyService$$EnhancerBySpringCGLIB$$c673c675.displayThings(&lt;generated&gt;)</strike><sup>(aop)</sup>
  at <strike>sun.reflect.GeneratedMethodAccessor647.invoke(Unknown Source)</strike><sup>(aop)</sup>
  at <i>sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)</i><sup>(aop)</sup>
  at <i>java.lang.reflect.Method.invoke(Method.java:498)</i><sup>(aop)</sup>
  at <i>org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:205)</i><sup>(fwk)</sup>
  at <i>org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:133)</i><sup>(fwk)</sup>
  at <i>org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:116)</i><sup>(fwk)</sup>
  at <i>org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:827)</i><sup>(fwk)</sup>
  at <i>org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:738)</i><sup>(fwk)</sup>
  at <i>org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:85)</i><sup>(fwk)</sup>
  at <i>org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:963)</i><sup>(fwk)</sup>
  at <i>org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:897)</i><sup>(fwk)</sup>
  at <i>org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:970)</i><sup>(fwk)</sup>
  at <i>org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:861)</i><sup>(fwk)</sup>
  at <i>javax.servlet.http.HttpServlet.service(HttpServlet.java:624)</i><sup>(jee)</sup>
  at <i>org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:846)</i><sup>(fwk)</sup>
  at <i>javax.servlet.http.HttpServlet.service(HttpServlet.java:731)</i><sup>(jee)</sup>
  at <i>org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:303)</i><sup>(jee)</sup>
  at <i>org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:208)</i><sup>(jee)</sup>
  at <i>org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:52)</i><sup>(jee)</sup>
  at <i>org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:241)</i><sup>(jee)</sup>
  at <i>org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:208)</i><sup>(jee)</sup>
  ...
  at <i>org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:331)</i><sup>(fwk)</sup>
  at <i>org.springframework.security.web.FilterChainProxy.doFilterInternal(FilterChainProxy.java:214)</i><sup>(fwk)</sup>
  at <i>org.springframework.security.web.FilterChainProxy.doFilter(FilterChainProxy.java:177)</i><sup>(fwk)</sup>
  at <i>org.springframework.web.filter.DelegatingFilterProxy.invokeDelegate(DelegatingFilterProxy.java:346)</i><sup>(fwk)</sup>
  at <i>org.springframework.web.filter.DelegatingFilterProxy.doFilter(DelegatingFilterProxy.java:262)</i><sup>(fwk)</sup>
  at <i>org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:241)</i><sup>(jee)</sup>
  at <i>org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:208)</i><sup>(jee)</sup>
  ...
  at <i>org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:116)</i><sup>(jee)</sup>
  at <i>org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:436)</i><sup>(jee)</sup>
  at <i>org.apache.coyote.http11.AbstractHttp11Processor.process(AbstractHttp11Processor.java:1078)</i><sup>(jee)</sup>
  at <i>org.apache.coyote.AbstractProtocol$AbstractConnectionHandler.process(AbstractProtocol.java:625)</i><sup>(jee)</sup>
  at <i>org.apache.tomcat.util.net.JIoEndpoint$SocketProcessor.run(JIoEndpoint.java:316)</i><sup>(jee)</sup>
  at <i>java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)</i><sup>(jee)</sup>
  at <i>java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)</i><sup>(jee)</sup>
  at <i>org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)</i><sup>(jee)</sup>
  at <i>java.lang.Thread.run(Thread.java:748)</i><sup>(jee)</sup>
  ...
Caused by: <b>com.xyz.MyApp$HttpStack$HttpError</b>: <strike>I/O error on GET http://dummy/user/alice/things</strike><sup>(msg)</sup>
  at <b>com.xyz.MyApp$HttpStack.get(MyApp.java:40)</b>
  at <b>com.xyz.MyApp$MyClient.getTheThings(MyApp.java:24)</b>
  ... 23 common frames omitted
Caused by: <b>java.net.SocketTimeoutException</b>: <strike>Read timed out</strike><sup>(msg)</sup>
  at <b>com.xyz.MyApp$HttpStack.get(MyApp.java:38)</b>
  ... 24 common frames omitted
</pre>

---

<strike>Strike out elements</strike> may vary from one occurrence to the other:

* <strike>error messages</strike><sup>(msg)</sup> often contain stuff related to the very error occurrence context,
* <strike>AOP generated classes</strike><sup>(aop)</sup> may vary from one execution to another.

*Italic* elements are somewhat not stable, or at least useless (purely technical). Ex:

* <i>JEE container stuff</i><sup>(jee)</sup>: may change when you upgrade your JEE container version or add/remove/reorganize your servlet filters chain for instance,
* <i>Spring Framework</i><sup>(fwk)</sup> underlying stacks (MVC, security) for pretty much the same reason,
* <i>AOP and dynamic invocation</i><sup>(aop)</sup>: purely technical, and quite implementation-dependent.

Only **bolded elements** are supposed to be stable.

---

### Now let's consider error stack 2

(shortened)

<pre>
<b>com.xyz.MyApp$MyClient$MyClientException</b>: <strike>An error occurred while getting <mark>Bob</mark>'s things</strike><sup>(msg)</sup>
  at <b>com.xyz.MyApp$MyClient.getTheThings(MyApp.java:26)</b>
  at <b>com.xyz.MyApp$MyService.displayThings(MyApp.java:16)</b>
  at <strike>com.xyz.MyApp$MyService$$FastClassByCGLIB$$<mark>07e70d1e</mark>.invoke()</strike><sup>(aop)</sup>
  at <i>net.sf.cglib.proxy.MethodProxy.invoke()</i><sup>(aop)</sup>
  at <i>org.springframework.aop.framework.Cglib2AopProxy$CglibMethodInvocation.invokeJoinpoint()</i><sup>(fwk)</sup>
  at <i>org.springframework.aop.framework.ReflectiveMethodInvocation.proceed()</i><sup>(fwk)</sup>
  at <i>org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint.proceed()</i><sup>(fwk)</sup>
  at <i>sun.reflect.NativeMethodAccessorImpl.invoke0()</i><sup>(aop)</sup>
  at <i>sun.reflect.NativeMethodAccessorImpl.invoke()</i><sup>(aop)</sup>
  at <i>sun.reflect.DelegatingMethodAccessorImpl.invoke()</i><sup>(aop)</sup>
  at <i>java.lang.reflect.Method.invoke()</i><sup>(aop)</sup>
  at <i>org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethodWithGivenArgs()</i><sup>(fwk)</sup>
  at <i>org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethod()</i><sup>(fwk)</sup>
  at <i>org.springframework.aop.aspectj.AspectJAroundAdvice.invoke()</i><sup>(fwk)</sup>
  at <i>org.springframework.aop.framework.ReflectiveMethodInvocation.proceed()</i><sup>(fwk)</sup>
  at <i>org.springframework.aop.interceptor.AbstractTraceInterceptor.invoke()</i><sup>(fwk)</sup>
  at <i>org.springframework.aop.framework.ReflectiveMethodInvocation.proceed()</i><sup>(fwk)</sup>
  at <i>org.springframework.transaction.interceptor.TransactionInterceptor.invoke()</i><sup>(fwk)</sup>
  at <i>org.springframework.aop.framework.ReflectiveMethodInvocation.proceed()</i><sup>(fwk)</sup>
  at <i>org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke()</i><sup>(fwk)</sup>
  at <i>org.springframework.aop.framework.ReflectiveMethodInvocation.proceed()</i><sup>(fwk)</sup>
  at <i>org.springframework.aop.framework.Cglib2AopProxy$DynamicAdvisedInterceptor.intercept()</i><sup>(fwk)</sup>
  at <strike>com.xyz.MyApp$MyService$$EnhancerBySpringCGLIB$$<mark>e3f570b1</mark>.displayThings(&lt;generated&gt;)</strike><sup>(aop)</sup>
  at <strike>sun.reflect.<mark>GeneratedMethodAccessor737</mark>.invoke(Unknown Source)</strike><sup>(aop)</sup>
  ...
Caused by: <b>com.xyz.MyApp$HttpStack$HttpError</b>: <strike>I/O error on GET http://dummy/user/<mark>bob</mark>/things</strike><sup>(msg)</sup>
  at <b>com.xyz.MyApp$HttpStack.get(MyApp.java:40)</b>
  at <b>com.xyz.MyApp$MyClient.getTheThings(MyApp.java:24)</b>
  ... 23 common frames omitted
Caused by: <b>java.net.SocketTimeoutException</b>: <strike>Read timed out</strike><sup>(msg)</sup>
  at <b>com.xyz.MyApp$HttpStack.get(MyApp.java:38)</b>
  ... 24 common frames omitted
</pre>



You may see in this example that most of the <strike>striked elements</strike> are different from error stack
1 (messages and generated classes names).

Nevertheless it is the same exact error (despite the context is different as it applies to another user), and the goal
here is to be able to count them as *two occurrences of the same error*.

### Now let's consider error stack 3

(shortened)

<pre>
<b>com.xyz.MyApp$MyClient$MyClientException</b>: <strike>An error occurred while getting Alice's things</strike><sup>(msg)</sup>
  at <b>com.xyz.MyApp$MyClient.getTheThings(MyApp.java:26)</b>
  at <b>com.xyz.MyApp$MyService.displayThings(MyApp.java:16)</b>
  at <strike>com.xyz.MyApp$MyService$$FastClassByCGLIB$$e7645040.invoke()</strike><sup>(aop)</sup>
  at <i>net.sf.cglib.proxy.MethodProxy.invoke()</i><sup>(aop)</sup>
  at <i>org.springframework.aop.framework.Cglib2AopProxy$CglibMethodInvocation.invokeJoinpoint()</i><sup>(fwk)</sup>
  at <i>org.springframework.aop.framework.ReflectiveMethodInvocation.proceed()</i><sup>(fwk)</sup>
  at <i>org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint.proceed()</i><sup>(fwk)</sup>
  at <i>sun.reflect.NativeMethodAccessorImpl.invoke0()</i><sup>(aop)</sup>
  at <i>sun.reflect.NativeMethodAccessorImpl.invoke()</i><sup>(aop)</sup>
  at <i>sun.reflect.DelegatingMethodAccessorImpl.invoke()</i><sup>(aop)</sup>
  at <i>java.lang.reflect.Method.invoke()</i><sup>(aop)</sup>
  ...
Caused by: <b>com.xyz.MyApp$HttpStack$HttpError</b>: <strike>I/O error on GET http://dummy/user/alice/things</strike><sup>(msg)</sup>
  at <b>com.xyz.MyApp$HttpStack.get(MyApp.java:40)</b>
  at <b>com.xyz.MyApp$MyClient.getTheThings(MyApp.java:24)</b>
  ... 23 common frames omitted
Caused by: <b>javax.net.ssl.SSLException</b>: <strike>Connection has been shutdown: javax.net.ssl.SSLHandshakeException: Received fatal alert: certificate_unknown</strike><sup>(msg)</sup>
  at <b>com.sun.net.ssl.internal.ssl.SSLSocketImpl.checkEOF(SSLSocketImpl.java:1172)</b>
  ... 24 common frames omitted
</pre>

Here, you can see that the first and second errors are the same as in error stack 1, but the root cause is different (`SSLException` instead of `SocketTimeoutException`).

So in that case we don't want the top error hash computed for error stack 3 to be the same as for error stack 1.

As a conclusion, error hash computation applies the following rules:

1. an error hash shall not compute against the error message
2. an error hash shall compute against it's parent cause (recurses)
3. in order to stabilize the error hash (over time and space), it's nice to be able to strip the stack trace from non-stable elements

---

## Recommended exclusion patterns

In a Spring Framework context, the following exclusion patterns seem pretty good:

```xml
  <throwableConverter class="com.orange.common.logging.logback.CustomThrowableConverterWithHash">
    <!-- generated class names -->
    <exclude>\$\$FastClassByCGLIB\$\$</exclude>
    <exclude>\$\$EnhancerBySpringCGLIB\$\$</exclude>
    <exclude>^sun\.reflect\..*\.invoke</exclude>
    <!-- JDK internals -->
    <exclude>^com\.sun\.</exclude>
    <exclude>^sun\.net\.</exclude>
    <!-- dynamic invocation -->
    <exclude>^net\.sf\.cglib\.proxy\.MethodProxy\.invoke</exclude>
    <exclude>^org\.springframework\.cglib\.</exclude>
    <exclude>^org\.springframework\.transaction\.</exclude>
    <exclude>^org\.springframework\.validation\.</exclude>
    <exclude>^org\.springframework\.app\.</exclude>
    <exclude>^org\.springframework\.aop\.</exclude>
    <exclude>^java\.lang\.reflect\.Method\.invoke</exclude>
    <!-- Spring plumbing -->
    <exclude>^org\.springframework\.ws\..*\.invoke</exclude>
    <exclude>^org\.springframework\.ws\.transport\.</exclude>
    <exclude>^org\.springframework\.ws\.soap\.saaj\.SaajSoapMessage\.</exclude>
    <exclude>^org\.springframework\.ws\.client\.core\.WebServiceTemplate\.</exclude>
    <exclude>^org\.springframework\.web\.filter\.</exclude>
    <!-- Tomcat internals -->
    <exclude>^org\.apache\.tomcat\.</exclude>
    <exclude>^org\.apache\.catalina\.</exclude>
    <exclude>^org\.apache\.coyote\.</exclude>
    <exclude>^java\.util\.concurrent\.ThreadPoolExecutor\.runWorker</exclude>
    <exclude>^java\.lang\.Thread\.run$</exclude>
  </throwableConverter>
```
