package thirdlab.sudoku.logging;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LogConfigTest {
    @Test
    void loadsTheClasspathConfiguration() {
        LogConfig.init();
        assertEquals(Level.FINE, Logger.getLogger("thirdlab.sudoku").getLevel());
    }

    @Test
    void warnsWhenTheConfigurationIsMissingAndFailsWhenItCannotBeRead() {
        LogConfig.init(null);
        InputStream broken = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("boom");
            }
        };
        assertThrows(IllegalStateException.class, () -> LogConfig.init(broken));
        LogConfig.init(new ByteArrayInputStream(".level=INFO\n".getBytes(StandardCharsets.UTF_8)));
    }
}
