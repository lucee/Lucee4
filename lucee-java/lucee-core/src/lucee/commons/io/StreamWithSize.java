package lucee.commons.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public interface StreamWithSize {
    StreamWithSize EMPTY =  new StreamWithSize() {
        private final InputStream emptyStream = new ByteArrayInputStream(new byte[]{});
        @Override
        public InputStream getInputStream() {
            return emptyStream;
        }

        @Override
        public long getSize() {
            return 0;
        }
    };

    InputStream getInputStream() throws IOException;
    long getSize();
}
