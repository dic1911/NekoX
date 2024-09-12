package tw.nekomimi.nekogram.transtale;

import org.dizitart.no2.collection.Document;
import org.dizitart.no2.common.mapper.NitriteMapper;
import org.dizitart.no2.repository.annotations.Id;
import org.dizitart.no2.repository.annotations.Index;

// @Index("chatId")
@Index(fields = {"chatId"})
public class ChatLanguage {

    // @Id
    @Id
    public long chatId;

    public String language;

    public ChatLanguage() {
    }

    public ChatLanguage(long chatId, String language) {
        this.chatId = chatId;
        this.language = language;
    }

    // @Override
    public Document write(NitriteMapper mapper) {
        Document document = Document.createDocument();
        document.put("chatId",chatId);
        document.put("language",language);
        return document;
    }

    // @Override
    public void read(NitriteMapper mapper, Document document) {
        chatId = ((long) document.get("chatId"));
        language = ((String) document.get("language"));
    }

}
