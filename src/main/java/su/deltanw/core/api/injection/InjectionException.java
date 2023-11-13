package su.deltanw.core.api.injection;

public class InjectionException extends RuntimeException {

  public InjectionException(String message) {
    super(message);
  }

  public InjectionException(Throwable cause) {
    super(cause);
  }
}