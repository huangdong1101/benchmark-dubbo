package com.mamba.benchmark.dubbo.filter;

import com.mamba.benchmark.common.util.TraceIdGenerator;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.ListenableFilter;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcContext;
import org.apache.dubbo.rpc.RpcException;
import org.apache.dubbo.rpc.service.GenericException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

@Activate(group = "consumer", order = Integer.MAX_VALUE)
public class MeasureFilter extends ListenableFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(MeasureFilter.class);

    private static final String KEY_TRACE_ID = "_RPC_TRACE_ID_";

    private static final String KEY_BEGIN_TIME = "_RPC_BEGIN_TIME_";

    private static final String TRACE_ID_SIGN = "ab";

    private static final Function<String, String> TRACE_ID_GENERATOR = k -> TraceIdGenerator.genTraceId(TRACE_ID_SIGN);

    public MeasureFilter() {
        super();
        super.listener = new MeasureListener();
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
        RpcContext.getContext().getAttachments().computeIfAbsent(KEY_TRACE_ID, TRACE_ID_GENERATOR);
        RpcContext.getContext().set(KEY_BEGIN_TIME, System.currentTimeMillis());
        LOGGER.info("Invoke");
        return invoker.invoke(invocation);
    }

    private static class MeasureListener implements Listener {

        @Override
        public void onResponse(Result result, Invoker<?> invoker, Invocation invocation) {
            long endTime = System.currentTimeMillis();
            long beginTime = (Long) RpcContext.getContext().get(KEY_BEGIN_TIME);
            long latency = endTime - beginTime;
            Throwable throwable = result.getException();
            if (throwable == null) { //正常响应
                //TODO: 响应
                result.getValue();
                LOGGER.info("Response {} ms, success", latency);
            } else { //异常
                if (throwable instanceof GenericException) {
                    LOGGER.info("Response {} ms, error: {} {} {}", latency, throwable.getClass().getSimpleName(), ((GenericException) throwable).getExceptionClass(), ((GenericException) throwable).getExceptionMessage());
                } else {
                    LOGGER.info("Response {} ms, error: {} {}", latency, throwable.getClass().getSimpleName(), throwable.getMessage());
                }
            }
        }

        @Override
        public void onError(Throwable throwable, Invoker<?> invoker, Invocation invocation) {
            if (throwable instanceof RpcException) {
                LOGGER.info("Error: {} {} {}", throwable.getClass().getSimpleName(), ((RpcException) throwable).getCode(), throwable.getMessage());
            } else {
                LOGGER.info("Error: {} {}", throwable.getClass().getSimpleName(), throwable.getMessage());
            }
        }
    }
}
