package tw.nekomimi.nekogram.transtale;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;

public class TransItemConverter implements EntityConverter<TransItem> {
    @Override
    public Class<TransItem> getEntityType() {
        return TransItem.class;
    }

    @Override
    public Document toDocument(TransItem entity, NitriteMapper nitriteMapper) {
        return Document.createDocument()
                .put("text", entity.text)
                .put("trans", entity.trans);
    }

    @Override
    public TransItem fromDocument(Document document, NitriteMapper nitriteMapper) {
        String text = (String) nitriteMapper.tryConvert(document.get("text", String.class), String.class);
        String trans = (String) nitriteMapper.tryConvert(document.get("trans", String.class), String.class);

        // ???
        if (text == null) text = "";
        if (trans == null) trans = "";

        return new TransItem(text, trans);
    }
}
