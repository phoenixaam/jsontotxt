public enum Value implements Loader {
    VALUE;
    public void load(){
        System.out.println("load");
    }

    public static void main(String[] args) {
        VALUE.load();
    }
  }
