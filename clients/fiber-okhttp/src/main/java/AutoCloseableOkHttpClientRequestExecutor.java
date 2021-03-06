import co.paralleluniverse.comsat.bench.http.client.AutoCloseableRequestExecutor;
import co.paralleluniverse.comsat.bench.http.client.ClientBase;
import co.paralleluniverse.fibers.RuntimeExecutionException;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.fibers.okhttp.FiberOkHttpClient;
import com.pinterest.jbender.executors.Validator;
import com.squareup.okhttp.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class AutoCloseableOkHttpClientRequestExecutor extends AutoCloseableRequestExecutor<Request, Response> {
  public static final Validator<Response> DEFAULT_VALIDATOR = (Response r) -> {
    if (!r.isSuccessful())
      throw new AssertionError("Request didn't complete successfully");
  };

  protected final Validator<Response> validator;
  protected final OkHttpClient client;

  public AutoCloseableOkHttpClientRequestExecutor(OkHttpClient client, Validator<Response> resValidator, int maxConnections, int timeout) {
    this.validator = resValidator;
    this.client = client;

    this.client.getDispatcher().setMaxRequests(maxConnections);
    this.client.getDispatcher().setMaxRequestsPerHost(maxConnections);

    this.client.setRetryOnConnectionFailure(false);

    this.client.setConnectTimeout(timeout, TimeUnit.MILLISECONDS);
    this.client.setReadTimeout(timeout, TimeUnit.MILLISECONDS);
    this.client.setWriteTimeout(timeout, TimeUnit.MILLISECONDS);
  }

  public AutoCloseableOkHttpClientRequestExecutor(Validator<Response> resValidator, int maxReqs, int timeout) {
    this(new FiberOkHttpClient(), resValidator, maxReqs, timeout);
  }

  public Response execute0(long nanoTime, Request request) throws InterruptedException, SuspendExecution {
    Response ret;
    try {
      ret = client.newCall(request).execute();
      ret.body().close();
    } catch (final IOException e) {
      throw new RuntimeExecutionException(e);
    }

    ClientBase.validate(validator, ret);

    return ret;
  }

  public void close() throws IOException {
    // client.getConnectionPool().evictAll();
    client.getDispatcher().getExecutorService().shutdown();
  }
}
