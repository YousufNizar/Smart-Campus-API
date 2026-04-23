package smartcampus.servlet;

import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Binds only to /v1 and /v1/* so this wins over Jersey's / * mapping.
 * Redirects to /api/v1? so the main JAX-RS app handles the request.
 */
public class V1RedirectServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        sendRedirect(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        sendRedirect(req, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        sendRedirect(req, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        sendRedirect(req, resp);
    }

    private void sendRedirect(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String ctx = req.getContextPath();
        String afterCtx = req.getRequestURI().substring(ctx.length());
        String target = ctx + "/api" + afterCtx;
        String qs = req.getQueryString();
        if (qs != null) {
            target = target + "?" + qs;
        }
        resp.sendRedirect(resp.encodeRedirectURL(target));
    }
}
