package org.digit.notify.app.plugin;

import org.digit.notify.app.domain.entity.ProviderEntity;
import org.digit.notify.app.domain.repository.ProviderRepository;
import org.digit.notify.spi.Channel;
import org.digit.notify.spi.ChannelMessage;
import org.digit.notify.spi.DispatchResult;
import org.digit.notify.spi.NotificationChannelProvider;
import org.digit.notify.spi.Recipient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PluginLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldDiscoverAndRegisterProviderFromJar() throws Exception {
        Path jarPath = buildTestProviderJar(tempDir, "test-sms-provider.jar",
            "TestSmsProvider", "test-sms", Channel.SMS);

        ProviderRepository mockRepo = mock(ProviderRepository.class);
        when(mockRepo.findByProviderName("test-sms")).thenReturn(Optional.empty());
        when(mockRepo.save(any())).thenReturn(new ProviderEntity());

        var loader = new ProviderPluginLoader(mockRepo, tempDir.toString());
        loader.loadPlugins();

        var provider = loader.getProvider(Channel.SMS, "test-sms");
        assertThat(provider).isPresent();
        assertThat(provider.get().providerName()).isEqualTo("test-sms");
        assertThat(provider.get().supportedChannel()).isEqualTo(Channel.SMS);

        verify(mockRepo, times(1)).save(any());
    }

    @Test
    void shouldWarnAndContinueWhenJarHasNoProviders() throws Exception {
        // Write empty jar (no META-INF/services)
        Path emptyJar = tempDir.resolve("empty-provider.jar");
        try (var jos = new JarOutputStream(new FileOutputStream(emptyJar.toFile()))) {
            jos.putNextEntry(new JarEntry("META-INF/"));
            jos.closeEntry();
        }

        ProviderRepository mockRepo = mock(ProviderRepository.class);
        var loader = new ProviderPluginLoader(mockRepo, tempDir.toString());
        loader.loadPlugins(); // must not throw

        for (Channel channel : Channel.values()) {
            assertThat(loader.getProvider(channel, "any")).isEmpty();
        }
        verify(mockRepo, never()).save(any());
    }

    @Test
    void shouldSkipMissingProviderNamesInGetProvidersOrdered() throws Exception {
        Path jarPath = buildTestProviderJar(tempDir, "test-sms2.jar",
            "TestSmsProvider2", "registered-provider", Channel.SMS);

        ProviderRepository mockRepo = mock(ProviderRepository.class);
        when(mockRepo.findByProviderName("registered-provider")).thenReturn(Optional.empty());
        when(mockRepo.save(any())).thenReturn(new ProviderEntity());

        var loader = new ProviderPluginLoader(mockRepo, tempDir.toString());
        loader.loadPlugins();

        var providers = loader.getProvidersOrdered(
            Channel.SMS, List.of("registered-provider", "missing-provider"));
        assertThat(providers).hasSize(1);
        assertThat(providers.get(0).providerName()).isEqualTo("registered-provider");
    }

    private Path buildTestProviderJar(Path dir, String jarName,
        String className, String providerName, Channel channel) throws Exception {
        // Write provider source inline using pre-compiled bytecode approach via URLClassLoader trick:
        // We create a provider class at runtime using a helper that writes raw bytecode.
        // For simplicity, we use a simpler approach: write the source, compile, then jar it.

        Path srcDir = dir.resolve("src");
        Files.createDirectories(srcDir);

        // Write source file
        String source = """
            import org.digit.notify.spi.*;
            import java.util.Map;
            public class %s implements NotificationChannelProvider {
                @Override public Channel supportedChannel() { return Channel.%s; }
                @Override public String providerName() { return "%s"; }
                @Override public DispatchResult send(ChannelMessage m, Recipient r, Map<String,Object> meta) {
                    return DispatchResult.dispatched(Channel.%s, "%s");
                }
            }
            """.formatted(className, channel.name(), providerName, channel.name(), providerName);

        Path srcFile = srcDir.resolve(className + ".java");
        Files.writeString(srcFile, source);

        // Get SPI jar path from current classloader
        String spiJarPath = findSpiJarPath();

        // Compile
        javax.tools.JavaCompiler compiler = javax.tools.ToolProvider.getSystemJavaCompiler();
        if (compiler == null) throw new IllegalStateException("No system Java compiler available");

        int result = compiler.run(null, null, null,
            "--enable-preview",
            "--release", "25",
            "-classpath", spiJarPath,
            "-d", srcDir.toString(),
            srcFile.toString());
        if (result != 0) throw new IllegalStateException("Compilation failed for " + className);

        // Create jar with META-INF/services entry
        Path jarPath = dir.resolve(jarName);
        String serviceEntry = "META-INF/services/org.digit.notify.spi.NotificationChannelProvider";

        try (var jos = new JarOutputStream(new FileOutputStream(jarPath.toFile()))) {
            // Add class file
            Path classFile = srcDir.resolve(className + ".class");
            jos.putNextEntry(new JarEntry(className + ".class"));
            jos.write(Files.readAllBytes(classFile));
            jos.closeEntry();

            // Add services file
            jos.putNextEntry(new JarEntry("META-INF/"));
            jos.closeEntry();
            jos.putNextEntry(new JarEntry("META-INF/services/"));
            jos.closeEntry();
            jos.putNextEntry(new JarEntry(serviceEntry));
            jos.write(className.getBytes());
            jos.closeEntry();
        }

        return jarPath;
    }

    private String findSpiJarPath() {
        // Find notify-spi jar from current classloader
        var cl = Thread.currentThread().getContextClassLoader();
        if (cl instanceof URLClassLoader ucl) {
            for (var url : ucl.getURLs()) {
                if (url.getPath().contains("notify-spi")) return url.getPath();
            }
        }

        // Fallback: search Maven local repo
        String home = System.getProperty("user.home");
        Path spiJar = Path.of(home, ".m2", "repository", "org", "digit", "notify",
            "notify-spi", "1.0.0-SNAPSHOT", "notify-spi-1.0.0-SNAPSHOT.jar");
        if (Files.exists(spiJar)) return spiJar.toString();

        // Try test classpath
        String cp = System.getProperty("java.class.path", "");
        for (String entry : cp.split(File.pathSeparator)) {
            if (entry.contains("notify-spi")) return entry;
        }

        throw new IllegalStateException("Cannot find notify-spi jar on classpath");
    }
}
