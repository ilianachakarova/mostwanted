package mostwanted.service.impl;

import mostwanted.common.Constants;
import mostwanted.domain.dtos.EntriesImportDto;
import mostwanted.domain.dtos.RaceImportDto;
import mostwanted.domain.dtos.RaceImportRootDto;
import mostwanted.domain.entities.District;
import mostwanted.domain.entities.Race;
import mostwanted.domain.entities.RaceEntry;
import mostwanted.repository.DistrictRepository;
import mostwanted.repository.RaceEntryRepository;
import mostwanted.repository.RaceRepository;
import mostwanted.service.RaceService;
import mostwanted.util.FileUtil;
import mostwanted.util.ValidationUtil;
import mostwanted.util.XmlParser;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class RaceServiceImpl implements RaceService {
    private final RaceRepository raceRepository;
    private final FileUtil fileUtil;
    private final XmlParser xmlParser;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;
    private final DistrictRepository districtRepository;
    private final RaceEntryRepository raceEntryRepository;

    private final static String RACES_XML_FILE_PATH = "src/main/resources/files/races.xml";
    @Autowired
    public RaceServiceImpl(RaceRepository raceRepository, FileUtil fileUtil, XmlParser xmlParser, ValidationUtil validationUtil, ModelMapper modelMapper, DistrictRepository districtRepository, RaceEntryRepository raceEntryRepository) {
        this.raceRepository = raceRepository;
        this.fileUtil = fileUtil;
        this.xmlParser = xmlParser;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
        this.districtRepository = districtRepository;
        this.raceEntryRepository = raceEntryRepository;
    }

    @Override
    public Boolean racesAreImported() {
        return this.raceRepository.count()>0;
    }

    @Override
    public String readRacesXmlFile() throws IOException {
        return this.fileUtil.readFile(RACES_XML_FILE_PATH);
    }

    @Override
    public String importRaces() throws JAXBException, FileNotFoundException {
    StringBuilder importResult = new StringBuilder();
        RaceImportRootDto raceImportRootDto = this.xmlParser.parseXml(RaceImportRootDto.class,RACES_XML_FILE_PATH);
        List<RaceImportDto> raceImportDtos = raceImportRootDto.getRaces();

        for (RaceImportDto raceImportDto : raceImportDtos) {
            Race race = this.raceRepository.findByDistrict_Name(raceImportDto.getDistrict()).orElse(null);
            int counter = 0;

            if(race != null){
                importResult.append(Constants.DUPLICATE_DATA_MESSAGE).append(System.lineSeparator());
                continue;
            }
            District district = this.districtRepository.findByName(raceImportDto.getDistrict()).orElse(null);
            if(!this.validationUtil.isValid(raceImportDto) || district == null){
                importResult.append(Constants.INCORRECT_DATA_MESSAGE).append(System.lineSeparator());
                continue;
            }
            race = modelMapper.map(raceImportDto,Race.class);
            race.setDistrict(district);

            List<EntriesImportDto>entriesImportDtos = raceImportDto.getEntryImportRootDto().getEntries();
            race.setRaceEntries(this.setRaceEntriesValues(entriesImportDtos));
            this.raceRepository.saveAndFlush(race);
            importResult.append(String.format(Constants.SUCCESSFUL_IMPORT_MESSAGE,"Race",String.valueOf(counter++)));
        }
        return importResult.toString().trim();
    }

    private List<RaceEntry> setRaceEntriesValues(List<EntriesImportDto> entriesImportDtos) {
        List<RaceEntry> raceEntries = new ArrayList<>();
        for (EntriesImportDto entriesImportDto : entriesImportDtos) {
            RaceEntry raceEntry = this.raceEntryRepository.findById(entriesImportDto.getId()).orElse(null);
            if(raceEntry == null){
                System.out.println(Constants.INCORRECT_DATA_MESSAGE);
                continue;
            }
            raceEntry = this.modelMapper.map(entriesImportDto, RaceEntry.class);
            raceEntries.add(raceEntry);
            this.raceEntryRepository.saveAndFlush(raceEntry);
        }
        return raceEntries;
    }
}
