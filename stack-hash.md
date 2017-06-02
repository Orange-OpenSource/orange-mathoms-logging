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

<style>
.stable {
    font-weight: bold;
}
.variable {
    text-decoration: line-through;
}
.variable.message {
    color: #FF4136;
}
.variable.aop {
    color: #85144b;
}
.technical {
    font-style: italic;
}
.technical.aop {
    color: #FF851B;
}
.technical.fwk {
    color: #3D9970;
}
.technical.jee {
    color: #0074D9;
}
.changed {
    font-weight: bold;
    background-color: #7FDBFF;
}
</style>

### Let's consider error stack 1

Note: the stack trace presented here has been cut by half from useless lines

<pre>
<span class=stable>com.xyz.MyApp$MyClient$MyClientException</span>: <span class="variable message">An error occurred while getting Alice's things</span>
  at <span class=stable>com.xyz.MyApp$MyClient.getTheThings(MyApp.java:26)</span>
  at <span class=stable>com.xyz.MyApp$MyService.displayThings(MyApp.java:16)</span>
  at <span class="variable aop">com.xyz.MyApp$MyService$$FastClassByCGLIB$$e7645040.invoke()</span>
  at <span class="technical aop">net.sf.cglib.proxy.MethodProxy.invoke()</span>
  at <span class="technical fwk">org.springframework.aop.framework.Cglib2AopProxy$CglibMethodInvocation.invokeJoinpoint()</span>
  at <span class="technical fwk">org.springframework.aop.framework.ReflectiveMethodInvocation.proceed()</span>
  at <span class="technical fwk">org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint.proceed()</span>
  at <span class="technical aop">sun.reflect.NativeMethodAccessorImpl.invoke0()</span>
  at <span class="technical aop">sun.reflect.NativeMethodAccessorImpl.invoke()</span>
  at <span class="technical aop">sun.reflect.DelegatingMethodAccessorImpl.invoke()</span>
  at <span class="technical aop">java.lang.reflect.Method.invoke()</span>
  at <span class="technical fwk">org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethodWithGivenArgs()</span>
  at <span class="technical fwk">org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethod()</span>
  at <span class="technical fwk">org.springframework.aop.aspectj.AspectJAroundAdvice.invoke()</span>
  at <span class="technical fwk">org.springframework.aop.framework.ReflectiveMethodInvocation.proceed()</span>
  at <span class="technical fwk">org.springframework.aop.interceptor.AbstractTraceInterceptor.invoke()</span>
  at <span class="technical fwk">org.springframework.aop.framework.ReflectiveMethodInvocation.proceed()</span>
  at <span class="technical fwk">org.springframework.transaction.interceptor.TransactionInterceptor.invoke()</span>
  at <span class="technical fwk">org.springframework.aop.framework.ReflectiveMethodInvocation.proceed()</span>
  at <span class="technical fwk">org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke()</span>
  at <span class="technical fwk">org.springframework.aop.framework.ReflectiveMethodInvocation.proceed()</span>
  at <span class="technical fwk">org.springframework.aop.framework.Cglib2AopProxy$DynamicAdvisedInterceptor.intercept()</span>
  at <span class="variable aop">com.xyz.MyApp$MyService$$EnhancerBySpringCGLIB$$c673c675.displayThings(&lt;generated&gt;)</span>
  at <span class="variable aop">sun.reflect.GeneratedMethodAccessor647.invoke(Unknown Source)</span>
  at <span class="technical aop">sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)</span>
  at <span class="technical aop">java.lang.reflect.Method.invoke(Method.java:498)</span>
  at <span class="technical fwk">org.springframework.web.method.support.InvocableHandlerMethod.doInvoke(InvocableHandlerMethod.java:205)</span>
  at <span class="technical fwk">org.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:133)</span>
  at <span class="technical fwk">org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:116)</span>
  at <span class="technical fwk">org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandlerMethod(RequestMappingHandlerAdapter.java:827)</span>
  at <span class="technical fwk">org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:738)</span>
  at <span class="technical fwk">org.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:85)</span>
  at <span class="technical fwk">org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:963)</span>
  at <span class="technical fwk">org.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:897)</span>
  at <span class="technical fwk">org.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:970)</span>
  at <span class="technical fwk">org.springframework.web.servlet.FrameworkServlet.doGet(FrameworkServlet.java:861)</span>
  at <span class="technical jee">javax.servlet.http.HttpServlet.service(HttpServlet.java:624)</span>
  at <span class="technical fwk">org.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:846)</span>
  at <span class="technical jee">javax.servlet.http.HttpServlet.service(HttpServlet.java:731)</span>
  at <span class="technical jee">org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:303)</span>
  at <span class="technical jee">org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:208)</span>
  at <span class="technical jee">org.apache.tomcat.websocket.server.WsFilter.doFilter(WsFilter.java:52)</span>
  at <span class="technical jee">org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:241)</span>
  at <span class="technical jee">org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:208)</span>
  ...
  at <span class="technical fwk">org.springframework.security.web.FilterChainProxy$VirtualFilterChain.doFilter(FilterChainProxy.java:331)</span>
  at <span class="technical fwk">org.springframework.security.web.FilterChainProxy.doFilterInternal(FilterChainProxy.java:214)</span>
  at <span class="technical fwk">org.springframework.security.web.FilterChainProxy.doFilter(FilterChainProxy.java:177)</span>
  at <span class="technical fwk">org.springframework.web.filter.DelegatingFilterProxy.invokeDelegate(DelegatingFilterProxy.java:346)</span>
  at <span class="technical fwk">org.springframework.web.filter.DelegatingFilterProxy.doFilter(DelegatingFilterProxy.java:262)</span>
  at <span class="technical jee">org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:241)</span>
  at <span class="technical jee">org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:208)</span>
  ...
  at <span class="technical jee">org.apache.catalina.core.StandardEngineValve.invoke(StandardEngineValve.java:116)</span>
  at <span class="technical jee">org.apache.catalina.connector.CoyoteAdapter.service(CoyoteAdapter.java:436)</span>
  at <span class="technical jee">org.apache.coyote.http11.AbstractHttp11Processor.process(AbstractHttp11Processor.java:1078)</span>
  at <span class="technical jee">org.apache.coyote.AbstractProtocol$AbstractConnectionHandler.process(AbstractProtocol.java:625)</span>
  at <span class="technical jee">org.apache.tomcat.util.net.JIoEndpoint$SocketProcessor.run(JIoEndpoint.java:316)</span>
  at <span class="technical jee">java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)</span>
  at <span class="technical jee">java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)</span>
  at <span class="technical jee">org.apache.tomcat.util.threads.TaskThread$WrappingRunnable.run(TaskThread.java:61)</span>
  at <span class="technical jee">java.lang.Thread.run(Thread.java:748)</span>
  ...
Caused by: <span class=stable>com.xyz.MyApp$HttpStack$HttpError</span>: <span class="variable message">I/O error on GET http://dummy/user/alice/things</span>
  at <span class=stable>com.xyz.MyApp$HttpStack.get(MyApp.java:40)</span>
  at <span class=stable>com.xyz.MyApp$MyClient.getTheThings(MyApp.java:24)</span>
  ... 23 common frames omitted
Caused by: <span class=stable>java.net.SocketTimeoutException</span>: <span class="variable message">Read timed out</span>
  at <span class=stable>com.xyz.MyApp$HttpStack.get(MyApp.java:38)</span>
  ... 24 common frames omitted
</pre>

---

<span class=variable>Striked out elements</span> may vary from one occurrence to the other:

* <span class="variable message">error messages</span> often contain stuff related to the very error occurrence context,
* <span class="variable aop">AOP and dynamic invocation / generated classes</span> may vary from one execution to another.

*Italic* elements are somewhat not stable, or at least useless (purely technical). Ex:

* <span class="technical jee">JEE container stuff</span>: may change when you upgrade your JEE container version or add/remove/reorganize your servlet filters chain for instance,
* <span class="technical fwk">Spring Framework</span> underlying stacks (MVC, security) for pretty much the same reason,
* <span class="technical aop">AOP and dynamic invocation</span>: purely technical, and quite implementation-dependent.

Only **bolded elements** are supposed to be stable.

---

### Now let's consider error stack 2

(shortened)

<pre>
<span class=stable>com.xyz.MyApp$MyClient$MyClientException</span>: <span class="variable message">An error occurred while getting <span class=changed>Bob</span>'s things</span>
  at <span class=stable>com.xyz.MyApp$MyClient.getTheThings(MyApp.java:26)</span>
  at <span class=stable>com.xyz.MyApp$MyService.displayThings(MyApp.java:16)</span>
  at <span class="variable aop">com.xyz.MyApp$MyService$$FastClassByCGLIB$$<span class=changed>07e70d1e</span>.invoke()</span>
  at <span class="technical aop">net.sf.cglib.proxy.MethodProxy.invoke()</span>
  at <span class="technical fwk">org.springframework.aop.framework.Cglib2AopProxy$CglibMethodInvocation.invokeJoinpoint()</span>
  at <span class="technical fwk">org.springframework.aop.framework.ReflectiveMethodInvocation.proceed()</span>
  at <span class="technical fwk">org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint.proceed()</span>
  at <span class="technical aop">sun.reflect.NativeMethodAccessorImpl.invoke0()</span>
  at <span class="technical aop">sun.reflect.NativeMethodAccessorImpl.invoke()</span>
  at <span class="technical aop">sun.reflect.DelegatingMethodAccessorImpl.invoke()</span>
  at <span class="technical aop">java.lang.reflect.Method.invoke()</span>
  at <span class="technical fwk">org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethodWithGivenArgs()</span>
  at <span class="technical fwk">org.springframework.aop.aspectj.AbstractAspectJAdvice.invokeAdviceMethod()</span>
  at <span class="technical fwk">org.springframework.aop.aspectj.AspectJAroundAdvice.invoke()</span>
  at <span class="technical fwk">org.springframework.aop.framework.ReflectiveMethodInvocation.proceed()</span>
  at <span class="technical fwk">org.springframework.aop.interceptor.AbstractTraceInterceptor.invoke()</span>
  at <span class="technical fwk">org.springframework.aop.framework.ReflectiveMethodInvocation.proceed()</span>
  at <span class="technical fwk">org.springframework.transaction.interceptor.TransactionInterceptor.invoke()</span>
  at <span class="technical fwk">org.springframework.aop.framework.ReflectiveMethodInvocation.proceed()</span>
  at <span class="technical fwk">org.springframework.aop.interceptor.ExposeInvocationInterceptor.invoke()</span>
  at <span class="technical fwk">org.springframework.aop.framework.ReflectiveMethodInvocation.proceed()</span>
  at <span class="technical fwk">org.springframework.aop.framework.Cglib2AopProxy$DynamicAdvisedInterceptor.intercept()</span>
  at <span class="variable aop">com.xyz.MyApp$MyService$$EnhancerBySpringCGLIB$$<span class=changed>e3f570b1</span>.displayThings(&lt;generated&gt;)</span>
  at <span class="variable aop">sun.reflect.<span class=changed>GeneratedMethodAccessor737</span>.invoke(Unknown Source)</span>
  ...
Caused by: <span class=stable>com.xyz.MyApp$HttpStack$HttpError</span>: <span class="variable message">I/O error on GET http://dummy/user/<span class=changed>bob</span>/things</span>
  at <span class=stable>com.xyz.MyApp$HttpStack.get(MyApp.java:40)</span>
  at <span class=stable>com.xyz.MyApp$MyClient.getTheThings(MyApp.java:24)</span>
  ... 23 common frames omitted
Caused by: <span class=stable>java.net.SocketTimeoutException</span>: <span class="variable message">Read timed out</span>
  at <span class=stable>com.xyz.MyApp$HttpStack.get(MyApp.java:38)</span>
  ... 24 common frames omitted
</pre>



You may see in this example that most of the <span class=variable>striked</span> elements are different from error stack
1 (messages and generated classes names).

Nevertheless it is the same exact error (despite the context is different as it applies to another user), and the goal
here is to be able to count them as *two occurrences of the same error*.

### Now let's consider error stack 3

(shortened)

<pre>
<span class=stable>com.xyz.MyApp$MyClient$MyClientException</span>: <span class="variable message">An error occurred while getting Alice's things</span>
  at <span class=stable>com.xyz.MyApp$MyClient.getTheThings(MyApp.java:26)</span>
  at <span class=stable>com.xyz.MyApp$MyService.displayThings(MyApp.java:16)</span>
  at <span class="variable aop">com.xyz.MyApp$MyService$$FastClassByCGLIB$$e7645040.invoke()</span>
  at <span class="technical aop">net.sf.cglib.proxy.MethodProxy.invoke()</span>
  at <span class="technical fwk">org.springframework.aop.framework.Cglib2AopProxy$CglibMethodInvocation.invokeJoinpoint()</span>
  at <span class="technical fwk">org.springframework.aop.framework.ReflectiveMethodInvocation.proceed()</span>
  at <span class="technical fwk">org.springframework.aop.aspectj.MethodInvocationProceedingJoinPoint.proceed()</span>
  at <span class="technical aop">sun.reflect.NativeMethodAccessorImpl.invoke0()</span>
  at <span class="technical aop">sun.reflect.NativeMethodAccessorImpl.invoke()</span>
  at <span class="technical aop">sun.reflect.DelegatingMethodAccessorImpl.invoke()</span>
  at <span class="technical aop">java.lang.reflect.Method.invoke()</span>
  ...
Caused by: <span class=stable>com.xyz.MyApp$HttpStack$HttpError</span>: <span class="variable message">I/O error on GET http://dummy/user/alice/things</span>
  at <span class=stable>com.xyz.MyApp$HttpStack.get(MyApp.java:40)</span>
  at <span class=stable>com.xyz.MyApp$MyClient.getTheThings(MyApp.java:24)</span>
  ... 23 common frames omitted
Caused by: <span class=stable>javax.net.ssl.SSLException</span>: <span class="variable message">Connection has been shutdown: javax.net.ssl.SSLHandshakeException: Received fatal alert: certificate_unknown</span>
  at <span class=stable>com.sun.net.ssl.internal.ssl.SSLSocketImpl.checkEOF(SSLSocketImpl.java:1172)</span>
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
