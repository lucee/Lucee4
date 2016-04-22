package lucee.runtime.net.ftp;

import lucee.runtime.exp.ApplicationException;

public class FTPException extends ApplicationException {

	private static final long serialVersionUID = -8239701967074210430L;
	
	public FTPException(String message) {
		super(message);
	}
	public FTPException(Exception e) {
		super(e.getMessage());
	}

}
