package de.freiburg.uni.tablet.presenter.xsocket;

import org.xsocket.connection.IConnectHandler;
import org.xsocket.connection.IConnectionTimeoutHandler;
import org.xsocket.connection.IDataHandler;
import org.xsocket.connection.IDisconnectHandler;
import org.xsocket.connection.IIdleTimeoutHandler;

public interface ISystemHandler extends IConnectHandler, IDataHandler, IDisconnectHandler, IConnectionTimeoutHandler, IIdleTimeoutHandler {
	
}
