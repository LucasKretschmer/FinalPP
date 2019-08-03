/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.tecnicon.paginas;

import java.io.Serializable;
import javax.ejb.Stateless;

/**
 *
 * @author jean.siqueira
 */
@Stateless
public class Inicial implements Serializable {

    public String iniciar() {
        StringBuilder html = new StringBuilder();
        return html.toString();
    }
}
