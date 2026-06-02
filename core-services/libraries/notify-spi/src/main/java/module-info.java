module org.digit.notify.spi {
    requires org.slf4j;
    requires static jakarta.annotation;
    requires org.jspecify;

    exports org.digit.notify.spi;

    uses org.digit.notify.spi.NotificationChannelProvider;
}
