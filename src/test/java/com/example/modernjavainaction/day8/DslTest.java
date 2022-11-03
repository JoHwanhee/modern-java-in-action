package com.example.modernjavainaction.day8;


public class DslTest {



    class Order {
        private String customer;

        public void setCustomer(String customer) {
            this.customer = customer;
        }
    }

    class Trade {

        private Type type;
        private int quantity;

        public void setType(Type type) {
            this.type = type;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public int getQuantity() {
            return quantity;
        }

        public class Type {
        }
    }

    class ChainingOrderBuilder {
        public final Order order = new Order();

        private ChainingOrderBuilder(String customer) {
            order.setCustomer(customer);
        }
    }

    class TradeBuilder {
        private final ChainingOrderBuilder builder;
        public final Trade trade = new Trade();

        private TradeBuilder(ChainingOrderBuilder builder, Trade.Type type, int quantity) {
            this.builder = builder;
            trade.setType(type);
            trade.setQuantity(quantity);
        }

        public StockBuilder stock(String symbol) {
            return new StockBuilder(builder, trade, symbol);
        }
    }

    class StockBuilder {
        public StockBuilder(DslTest.ChainingOrderBuilder builder, DslTest.Trade trade, String symbol) {
        }
    }

}
