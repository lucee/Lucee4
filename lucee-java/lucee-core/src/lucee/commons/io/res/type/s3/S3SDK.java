package lucee.commons.io.res.type.s3;

import com.mastercontrol.resource.s3.S3Facade;
import com.mastercontrol.resource.s3.S3ListItem;
import lucee.commons.io.StreamWithSize;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class S3SDK {
    private S3Facade facade;

    public S3SDK() {
    }

    public List<S3ListItem> listContents(String prefix) {
        return new S3Facade(prefix)
                .listContents();
    }

    public boolean exists(String objectName) {
        return initializeIfNeeded(objectName)
                            .exists();
    }

    public boolean directoryExists(String objectName) {
        return initializeIfNeeded(objectName)
                        .directoryExists();
    }

    public boolean fileExists(String objectName) {
        return initializeIfNeeded(objectName)
                .fileExists();
    }

    public long getLastModified(String objectName) {
        return initializeIfNeeded(objectName)
                        .getLastModified();
    }

    public long getSize(String objectName) {
        return initializeIfNeeded(objectName)
                        .getLength();
    }

    public void put(String objectName, StreamWithSize stream) throws IOException {
        new S3Facade(objectName).putInputStream(stream.getInputStream(), stream.getSize());
    }

    public InputStream getInputStream(String objectName) {
        return new S3Facade(objectName).getInputStream();
    }

    public void delete(String objectName) {
        new S3Facade(objectName).delete();
    }

    private S3Facade initializeIfNeeded(String objectName) {
        if (facade == null) {
            facade = new S3Facade(objectName);
        }

        return facade;
    }
}
