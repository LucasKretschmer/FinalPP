package br.com.tecnicon.controller;

import br.com.tecnicon.objects.put.DadosEmpresa;
import br.com.tecnicon.paginas.Index;
import br.com.tecnicon.server.dataset.TClientDataSet;
import br.com.tecnicon.server.sessao.TVariavelSessao;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author jack
 */
@WebServlet(name = "PerfilNegocio", urlPatterns =
{
    "/PerfilNegocio/*"
})
public class PerfilNegocio extends HttpServlet
{

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try
        {
            //HttpSession session = request.getSession();
            
            TVariavelSessao vs = new TVariavelSessao();
            vs.addParametros("sessao", "-9876");

            vs.addParametros("USACONTEXT", "N");
            if (request.getPathInfo() != null && !request.getPathInfo().isEmpty() && !"/".equals(request.getPathInfo()))
            {
                String cempresa = carregaEmpresa(vs, request.getPathInfo());
                vs.addParametros("empresa", cempresa);
            }
            vs.addParametros("tipologin", "perfilnegocio");
            
            //session.setAttribute("vs", vs);
            ///vs.setParametrosSessao(vs.getListaParametroSessao());
            //RepositorioSession.getInstance().setAttribute(session.getId(), vs);
            //session.setAttribute("navegador", "chrome");
            //session.setAttribute("versao", "50");
            
            out.println(Index.retornaIndex("navegador", "false", "", "false", "perfilnegocio", request));
            out.close();
        } catch (Exception e)
        {
            out.println(e.getMessage());
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo()
    {
        return "Short description";
    }// </editor-fold>

    private String carregaEmpresa(TVariavelSessao vs, String pathInfo) throws Exception
    {
        TClientDataSet empresas = DadosEmpresa.getEmpresas(" PERFILNEGOCIO = '" + pathInfo.substring(1) + "'");
        return empresas.fieldByName("CEMPRESA").asString();
    }
}
