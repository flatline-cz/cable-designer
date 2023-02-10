package biz.flatsw.cabledesigner.generator.cables;

import biz.flatsw.cabledesigner.Services;
import biz.flatsw.cabledesigner.generator.TextFormatterBase;
import biz.flatsw.cabledesigner.model.defs.Color;

public class TextFormatter extends TextFormatterBase implements Cables.Formatter {
    private final static String PAGE_BOM = "========= Cables (%s) ============";

    @Override
    public void addCable(String cableName, String signalName, float wireSection, int length, Color color) {
        setPage(String.format(PAGE_BOM, Services.getDocumentProperties().getHarness()));
        output.append(cableName);
        output.append("\t");
        output.append(signalName);
        output.append("\t");
        output.append(wireSection);
        output.append("\t");
        output.append(length);
        if(color!=null) {
            output.append("\t");
            output.append(color.getCodes());
        }
        output.append('\n');
    }
}
