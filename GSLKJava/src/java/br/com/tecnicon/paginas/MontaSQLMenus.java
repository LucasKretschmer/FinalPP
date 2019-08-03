package br.com.tecnicon.paginas;

import br.com.tecnicon.server.dataset.TSQLDataSetFW;
import br.com.tecnicon.server.execoes.ExcecaoTecnicon;
import br.com.tecnicon.server.sessao.VariavelSessao;
import javax.ejb.Stateless;

/**
 *
 * @author mauricio.sipmann
 */
@Stateless
public class MontaSQLMenus
{

    public String retornaCds(VariavelSessao vs) throws ExcecaoTecnicon
    {
        String sql = obterTelaHtml(vs);
        TSQLDataSetFW con = TSQLDataSetFW.create(vs);
        con.commandText(sql);
        con.open();
        return con.jsonData();
    }

    public String obterTelaHtml(VariavelSessao vs)
    {
        String idModulo = vs.getParameter("idModulo");
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT X.WBS, X.WEBOK, X.SMENU, X.MENU, X.CMODULO, X.STELAPAI, X.ORDEM, X.tParametros, X.umregistro, X.tRel, X.tCl, X.tTipo, X.ID AS TID,")
                .append(" (CASE")
                .append("   WHEN X.CTPINTERFACE = '1' THEN (SELECT TABELA.TABELA FROM TABELA WHERE TABELA.STABELA=X.ID)")
                .append("   WHEN X.CTPINTERFACE = '2' THEN (X.ID)")
                .append("   WHEN X.CTPINTERFACE = '3' THEN (X.ID)")
                .append("   WHEN X.CTPINTERFACE = '5' THEN (SELECT ESPECIAL.MODULO FROM ESPECIAL WHERE ESPECIAL.CESPECIAL = X.ID)")
                .append("   WHEN X.CTPINTERFACE = '8' THEN (SELECT TABELAOBJETO.NOME FROM TABELAOBJETO WHERE TABELAOBJETO.STABELAOBJETO = X.ID)")
                .append("   ELSE X.MENU END")
                .append(" ) AS tb,")
                .append(" (CASE")
                .append(" WHEN X.CTPINTERFACE = '1' THEN (SELECT TABELA.width FROM TABELA WHERE TABELA.STABELA=X.ID)")
                .append(" WHEN X.CTPINTERFACE = '8' THEN (SELECT TABELAOBJETO.WIDTH FROM TABELAOBJETO WHERE TABELAOBJETO.STABELAOBJETO=X.ID)")
                .append(" ELSE 0 END")
                .append(" ) AS WIDTH,")
                .append(" (CASE")
                .append(" WHEN X.CTPINTERFACE = '1' THEN (SELECT TABELA.height FROM TABELA WHERE TABELA.STABELA=X.ID)")
                .append(" WHEN X.CTPINTERFACE = '8' THEN (SELECT TABELAOBJETO.HEIGHT FROM TABELAOBJETO WHERE TABELAOBJETO.STABELAOBJETO=X.ID)")
                .append(" ELSE 0 END")
                .append(" ) AS HEIGHT,")
                .append(" (CASE")
                .append("   WHEN X.CTPINTERFACE = '1' THEN 'T'")
                .append("   WHEN X.CTPINTERFACE = '2' THEN 'R'")
                .append("   WHEN X.CTPINTERFACE = '3' THEN 'G'")
                .append("   WHEN X.CTPINTERFACE = '5' THEN 'E'")
                .append("   ELSE X.MENU END")
                .append(" ) AS TIPO")
                .append(" FROM")
                .append(" (")
                .append("   WITH RECURSIVE HIERARQUIA")
                .append("   AS")
                .append("   (SELECT DISTINCT")
                .append(" ")
                .append("    (TETIQUETA((M.ORDEM),3)) AS WBS,M.WEBOK, ")
                .append("     M.SMENU, M.MENU,M.STELAPAI, M.CMODULO,M.ORDEM,M.ID,M.CTPINTERFACE,M.PARAMETRO AS tParametros, M.UMREGISTRO, M.SRELATORIOALE AS TREL,M.CL AS TCL,M.CTPINTERFACE AS TTIPO, 0 AS NIVEL,")
                .append(" ")
                .append("    M.CL,M.SRELATORIOALE,")
                .append("    (SELECT COUNT (*) FROM MENU M1 WHERE M1.STELAPAI = M.SMENU) AS QT")
                .append("    FROM MENU M")
                .append("    WHERE M.CMODULO=")
                .append(idModulo)
                .append("    AND M.STELAPAI = 0")
                .append("    UNION ALL")
                .append("    SELECT")
                .append("    (H.WBS||'.'||TETIQUETA((M.ORDEM),3)) AS WBS, M.WEBOK,")
                .append("    M.SMENU, M.MENU,M.STELAPAI, M.CMODULO ,M.ORDEM,M.ID,M.CTPINTERFACE,M.PARAMETRO AS tParametros, M.UMREGISTRO,M.SRELATORIOALE AS TREL,M.CL AS TCL,M.CTPINTERFACE AS TTIPO,")
                .append("    H.NIVEL + 1,")
                .append(" ")
                .append("    M.CL,M.SRELATORIOALE,")
                .append("    (SELECT COUNT (*) FROM MENU M2 WHERE M2.STELAPAI = M.SMENU) AS QT")
                .append("    FROM HIERARQUIA H")
                .append("    JOIN MENU M ON H.SMENU = M.STELAPAI")
                .append("   )")
                .append("  SELECT *")
                .append("  FROM HIERARQUIA")
                .append(" ")
                .append(" ) AS X");
        if (vs.getParameter("MOBILE") != null && vs.getParameter("MOBILE").equals("S"))
        {
            sql.append(" WHERE X.CTPINTERFACE IN (").append(vs.getParameter("CTPINTERFACE")).append(")"); // 5 ESPECIAL - 3 GRAFICO
        }
        sql.append(" ORDER BY WBS");
        return sql.toString();
    }

}
