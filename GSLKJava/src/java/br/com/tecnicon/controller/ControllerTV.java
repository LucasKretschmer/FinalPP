package br.com.tecnicon.controller;

import br.com.tecnicon.criptografia.PWSec;
import br.com.tecnicon.server.RetornaCSSJS;
import br.com.tecnicon.server.context.TecniconLookup;
import br.com.tecnicon.server.dataset.TClientDataSet;
import br.com.tecnicon.server.sessao.TVariavelSessao;
import br.com.tecnicon.server.sessao.VariavelSessao;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author mauricio.sipmann
 */
@WebServlet(name = "TV", urlPatterns =
{
    "/TV"
})
public class ControllerTV extends HttpServlet
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
            TVariavelSessao vs = new TVariavelSessao();
            vs.addParametros("sessao", "-9876");

            Map<String, String[]> mp = vs.getParameterMap();
            String empresa = request.getParameter("empresa");

            mp.put("cobjetohtml", new String[]
            {
                "2971"
            });

            if (request.getParameter("CBANNER") == null && request.getParameter("s") != null)
            {
                try
                {
                    //Começa no indice 1 pois recebe primeiro um "&"
                    String tmp[] = PWSec.decrypt(request.getParameter("s")).split("&");
                    String cbanner = tmp[1].split("=")[1];
                    empresa = tmp[2].split("=")[1];
                    
                    mp.put("CBANNER", new String[]
                    {
                        cbanner
                    });
                } catch (BadPaddingException | NoSuchPaddingException | IllegalBlockSizeException | InvalidKeyException | NoSuchAlgorithmException | InvalidAlgorithmParameterException ex)
                {
                    return;
                }
            } else
            {
                mp.put("CBANNER", new String[]
                {
                    request.getParameter("CBANNER")
                });
            }

            vs.setParameterMap(mp);
            vs.addParametros("empresa", empresa);

            Object obj = TecniconLookup.lookup("BannerTV", "BannerTV");
            String conteudo = "<div id='conteudo-banner' empresa='" + vs.getValor("empresa") + "'>" + (String) obj.getClass().getMethod("geraHTML", VariavelSessao.class).invoke(obj, vs) + "</div>";

            TClientDataSet JAVASCRIPT = TClientDataSet.create(vs, "JAVASCRIPT");
            JAVASCRIPT.createDataSet();
            JAVASCRIPT.condicao("WHERE JAVASCRIPT.CJAVASCRIPT = 3477");
            JAVASCRIPT.open();

            String js = "<script>" + JAVASCRIPT.fieldByName("CODIGOJS").asString() + "</script>";

            obj = TecniconLookup.lookup("TecniconEspecialHTML", "ObjetosHTML");
            String ret = (String) obj.getClass().getMethod("retornaObjetoHTML", VariavelSessao.class).invoke(obj, vs);

            out.println(ret.replace("'{DADOS}'", conteudo).replace("'{JS}'", js) + "<script>classeJS.Init(document.querySelector('[role=dialog]'));</script>" + RetornaCSSJS.retornarCSSJS("desktop", false, false, "chrome", "37.0"));
        } catch (Exception ex)
        {
            out.println("erro:" + ex.getMessage());
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

}
