package nnu.wyz.fileMS.config;

import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Retryable(value = UncategorizedMongoDbException.class, exceptionExpression = "#{message.contains('WriteConflict error')}", maxAttempts = 128, backoff = @Backoff(delay = 500))
@Transactional(propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class, timeout = 120)
public @interface MongoTransactional {
}
