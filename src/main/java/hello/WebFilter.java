package hello;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;

@Configuration
public class WebFilter implements Filter {

	private static final Logger logger = LoggerFactory.getLogger(WebFilter.class);

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		logger.debug("Initiating WebFilter >> ");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest req = (HttpServletRequest) request;
		HeaderMapRequestWrapper requestWrapper = new HeaderMapRequestWrapper(req);
		String token = requestWrapper.getHeader("x-goog-iap-jwt-assertion");
		logger.info("In web filter");
		
		if (token != null) {
			logger.info("WebFilter token: " + token);
			try {
				DecodedJWT jwt = JWT.decode(token);
				logger.info("Token issuer: " + jwt.getIssuer() + " algorithm: " + jwt.getAlgorithm() + " key: " + jwt.getKeyId() + " signature: "
						+ jwt.getSignature() + " subject: " + jwt.getSubject() + " type: " + jwt.getType() + " audience: " + jwt.getAudience() + " subject: " + jwt.getSubject());
			} catch (JWTDecodeException exception) {
				logger.error("JWT Token decode failed: " + exception.getMessage());
				((HttpServletResponse) response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			}
			// Goes to default servlet
			chain.doFilter(requestWrapper, response);
		} else {
			logger.warn("Received request without JWT Token!");
			((HttpServletResponse) response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}

	@Override
	public void destroy() {
		logger.debug("Destroying WebFilter >> ");
	}
}