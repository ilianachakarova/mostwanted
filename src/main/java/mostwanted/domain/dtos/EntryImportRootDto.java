package mostwanted.domain.dtos;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "entries")
@XmlAccessorType(XmlAccessType.FIELD)
public class EntryImportRootDto {
    @XmlElement(name = "entry")
    private List<EntriesImportDto> entries;

    public EntryImportRootDto() {
    }

    public List<EntriesImportDto> getEntries() {
        return entries;
    }

    public void setEntries(List<EntriesImportDto> entries) {
        this.entries = entries;
    }
}
