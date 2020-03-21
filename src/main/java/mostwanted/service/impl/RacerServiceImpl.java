package mostwanted.service.impl;

import com.google.gson.Gson;
import mostwanted.common.Constants;
import mostwanted.domain.dtos.RacerImportDto;
import mostwanted.domain.entities.Racer;
import mostwanted.domain.entities.Town;
import mostwanted.repository.RacerRepository;
import mostwanted.repository.TownRepository;
import mostwanted.service.RacerService;
import mostwanted.util.FileUtil;
import mostwanted.util.ValidationUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class RacerServiceImpl implements RacerService {
    private final RacerRepository racerRepository;
    private final FileUtil fileUtil;
    private final Gson gson;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;
    private final TownRepository townRepository;


    private final static String RACERS_JSON_FILE_PATH = "src/main/resources/files/racers.json";
    @Autowired
    public RacerServiceImpl(RacerRepository racerRepository, FileUtil fileUtil, Gson gson, ValidationUtil validationUtil, ModelMapper modelMapper, TownRepository townRepository) {
        this.racerRepository = racerRepository;
        this.fileUtil = fileUtil;
        this.gson = gson;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
        this.townRepository = townRepository;
    }

    @Override
    public Boolean racersAreImported() {
        return this.racerRepository.count()>0;
    }

    @Override
    public String readRacersJsonFile() throws IOException {
        return this.fileUtil.readFile(RACERS_JSON_FILE_PATH);
    }

    @Override
    public String importRacers(String racersFileContent) {
        StringBuilder importResult = new StringBuilder();
        RacerImportDto[] racerImportDtos = this.gson.fromJson(racersFileContent,RacerImportDto[].class);
        for (RacerImportDto racerImportDto : racerImportDtos) {
            Racer racer = this.racerRepository.findByName(racerImportDto.getName()).orElse(null);
            if(racer != null){
                importResult.append(Constants.DUPLICATE_DATA_MESSAGE).append(System.lineSeparator());
                continue;
            }
            Town town = this.townRepository.getTownByName(racerImportDto.getHomeTown()).orElse(null);
            if(!this.validationUtil.isValid(racerImportDto) || town == null){
                importResult.append(Constants.INCORRECT_DATA_MESSAGE).append(System.lineSeparator());
                continue;
            }
            racer = this.modelMapper.map(racerImportDto, Racer.class);
            racer.setHomeTown(town);
            this.racerRepository.saveAndFlush(racer);
            importResult.append(
                    String.format(Constants.SUCCESSFUL_IMPORT_MESSAGE,"Racer",racer.getName())).append(System.lineSeparator());
        }
        return importResult.toString().trim();
    }

    @Override
    public String exportRacingCars() {
        StringBuilder exportResult = new StringBuilder();
        List<Racer> racers = this.racerRepository.exportRacingCars();
        for (Racer racer : racers) {
            exportResult.append(String.format("Name: %s",racer.getName())).append(System.lineSeparator());
            if(racer.getAge()!=null){
                exportResult.append(String.format("Age: %d",racer.getAge())).append(System.lineSeparator());
            }
            exportResult.append("Cars:").append(System.lineSeparator());
            racer.getCars().forEach(car -> {
                exportResult.
                        append(String.format("\t%s %s %d",car.getBrand(),car.getModel(),car.getYearOfProduction()))
                        .append(System.lineSeparator());
            });
            exportResult.append(System.lineSeparator());
        }

        return exportResult.toString().trim();
    }
}
