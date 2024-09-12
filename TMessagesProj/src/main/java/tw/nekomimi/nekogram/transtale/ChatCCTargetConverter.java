package tw.nekomimi.nekogram.transtale;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.EntityConverter;
import org.dizitart.no2.common.mapper.NitriteMapper;

public class ChatCCTargetConverter implements EntityConverter<ChatCCTarget> {
    @Override
    public Class<ChatCCTarget> getEntityType() {
        return ChatCCTarget.class;
    }

    @Override
    public Document toDocument(ChatCCTarget entity, NitriteMapper nitriteMapper) {
        return Document.createDocument()
                .put("chatId", entity.chatId)
                .put("ccTarget", entity.ccTarget);
    }

    @Override
    public ChatCCTarget fromDocument(Document document, NitriteMapper nitriteMapper) {
        Long chatId = (Long) nitriteMapper.tryConvert(document.get("chatId", Document.class), Long.class);
        String ccTarget = (String) nitriteMapper.tryConvert(document.get("ccTarget", Document.class), String.class);

        // ???
        if (chatId == null) chatId = 0L;
        if (ccTarget == null) ccTarget = "";

        return new ChatCCTarget(chatId, ccTarget);
    }
}
