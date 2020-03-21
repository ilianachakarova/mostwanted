package mostwanted.service.impl;

import mostwanted.common.Constants;
import mostwanted.domain.dtos.RaceEntryImportRootDto;
import mostwanted.domain.entities.Car;
import mostwanted.domain.entities.RaceEntry;
import mostwanted.domain.entities.Racer;
import mostwanted.repository.CarRepository;
import mostwanted.repository.RaceEntryRepository;
import mostwanted.repository.RacerRepository;
import mostwanted.service.RaceEntryService;
import mostwanted.util.FileUtil;
import mostwanted.util.ValidationUtil;
import mostwanted.util.XmlParser;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.io.IOException;

@Service
public class RaceEntryServiceImpl implements RaceEntryService {
    private final RaceEntryRepository raceEntryRepository;
    private final FileUtil fileUtil;
    private final ModelMapper modelMapper;
    private final XmlParser xmlParser;
    private final ValidationUtil validationUtil;
    private final RacerRepository racerRepository;
    private final CarRepository carRepository;


    private final static String RACE_ENTRIES_XML_FILE= "src/main/resources/files/race-entries.xml";
    @Autowired
    public RaceEntryServiceImpl(RaceEntryRepository raceEntryRepository, FileUtil fileUtil, ModelMapper modelMapper, XmlParser xmlParser, ValidationUtil validationUtil, RacerRepository racerRepository, CarRepository carRepository) {
        this.raceEntryRepository = raceEntryRepository;
        this.fileUtil = fileUtil;
        this.modelMapper = modelMapper;
        this.xmlParser = xmlParser;
        this.validationUtil = validationUtil;
        this.racerRepository = racerRepository;

        this.carRepository = carRepository;
    }


    @Override
    public Boolean raceEntriesAreImported() {
        return this.raceEntryRepository.count()>0;
    }

    @Override
    public String readRaceEntriesXmlFile() throws IOException {
        return this.fileUtil.readFile(RACE_ENTRIES_XML_FILE);
    }

    @Override
    public String importRaceEntries() throws JAXBException, FileNotFoundException {
        StringBuilder importResult = new StringBuilder();

        RaceEntryImportRootDto raceEntryImportRootDto = this.xmlParser
                .parseXml(RaceEntryImportRootDto.class, RACE_ENTRIES_XML_FILE);

        raceEntryImportRootDto.getRaceEntries().forEach(raceEntryImportDto -> {
            Racer racerEntity = this.racerRepository.findByName(raceEntryImportDto.getRacer()).orElse(null);
            Car carEntity = this.carRepository.findById(raceEntryImportDto.getCarId()).orElse(null);
            if (racerEntity == null || carEntity == null) {
                importResult.append(Constants.INCORRECT_DATA_MESSAGE).append(System.lineSeparator());

                return;
            }

            RaceEntry raceEntryEntity = this.modelMapper.map(raceEntryImportDto, RaceEntry.class);
            raceEntryEntity.setRacer(racerEntity);
            raceEntryEntity.setCar(carEntity);
            raceEntryEntity.setRace(null);
            raceEntryEntity = this.raceEntryRepository.saveAndFlush(raceEntryEntity);

            importResult
                    .append(String.format(Constants.SUCCESSFUL_IMPORT_MESSAGE, raceEntryEntity.getClass().getSimpleName(), raceEntryEntity.getId()))
                    .append(System.lineSeparator());
        });

        return importResult.toString().trim();
    }
}
