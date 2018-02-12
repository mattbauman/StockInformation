package ultimate;

public class ParseJSON {
    String inputText, attributeName, attributeValue;
    public String getAttributeValue(String a) {
        
        inputText = a;

        int attributeNameStartIndex=-1, attributeNameEndIndex=-1;
        int attributeValueStartIndex=-1, attributeValueEndIndex=-1;
        boolean attributeNameStartIndexFound=false, attributeNameEndIndexFound=false;
        boolean attributeValueStartIndexFound=false, attributeValueEndIndexFound=false;
        for(int i=0; i<inputText.length(); i++){
            if (String.valueOf(inputText.charAt(i)).equals("\"")) {
                if (!attributeNameStartIndexFound){
                    attributeNameStartIndex = i+1;
                    attributeNameStartIndexFound=true;
                } else if (!attributeNameEndIndexFound){
                    attributeNameEndIndex = i;
                    attributeNameEndIndexFound=true;
                } else if (!attributeValueStartIndexFound){
                    attributeValueStartIndex = i+1;
                    attributeValueStartIndexFound=true;
                } else if (!attributeValueEndIndexFound){
                    attributeValueEndIndex = i;
                    attributeValueEndIndexFound=true;
                }
            }
        }
       
        attributeName = inputText.substring(attributeNameStartIndex, attributeNameEndIndex);
        attributeValue = inputText.substring(attributeValueStartIndex, attributeValueEndIndex);
        return attributeValue;
        }


}

