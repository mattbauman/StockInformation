/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ultimate;


public class Ultimate {

    public static void main(String[] args) {

        Stock VFFVX = new Stock("VFFVX");
        VFFVX.getHistorical();
        VFFVX.createHistoricalStockJSON();

        Stock BBY = new Stock("BBY");
        BBY.getHistorical();
        BBY.createHistoricalStockJSON();

        Stock ACN = new Stock("ACN");
        ACN.getHistorical();
        ACN.createHistoricalStockJSON();

        Stock VOO = new Stock("VOO");
        VOO.getHistorical();
        VOO.createHistoricalStockJSON();


    }
}
