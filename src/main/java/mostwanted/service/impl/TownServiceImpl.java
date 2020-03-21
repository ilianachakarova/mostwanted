package mostwanted.service.impl;

import com.google.gson.Gson;
import mostwanted.common.Constants;
import mostwanted.domain.dtos.TownImportDto;
import mostwanted.domain.entities.Town;
import mostwanted.repository.TownRepository;
import mostwanted.service.TownService;
import mostwanted.util.FileUtil;
import mostwanted.util.ValidationUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class TownServiceImpl implements TownService {
    private final TownRepository townRepository;
    private final FileUtil fileUtil;
    private final Gson gson;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;

    private final static String TOWNS_JSON_FILE_PATH = "src/main/resources/files/towns.json";
    @Autowired
    public TownServiceImpl(TownRepository townRepository, FileUtil fileUtil, Gson gson, ValidationUtil validationUtil, ModelMapper modelMapper) {
        this.townRepository = townRepository;
        this.fileUtil = fileUtil;
        this.gson = gson;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
    }

    @Override
    public Boolean townsAreImported() {
        return this.townRepository.count()>0;
    }

    @Override
    public String readTownsJsonFile() throws IOException {
        return this.fileUtil.readFile(TOWNS_JSON_FILE_PATH);
    }

    @Override
    public String importTowns(String townsFileContent) {
        StringBuilder importResult =  new StringBuilder();
        TownImportDto[] townImportDtos = this.gson.fromJson(townsFileContent, TownImportDto[].class);
        for (TownImportDto townImportDto : townImportDtos) {
            Town town = this.townRepository.getTownByName(townImportDto.getName()).orElse(null);
            if(town != null){
                importResult.append(Constants.DUPLICATE_DATA_MESSAGE).append(System.lineSeparator());
                continue;
            }
            if(!this.validationUtil.isValid(townImportDto)){
                importResult.append(Constants.INCORRECT_DATA_MESSAGE).append(System.lineSeparator());
                continue;
            }
            town = this.modelMapper.map(townImportDto, Town.class);
            this.townRepository.saveAndFlush(town);
            importResult.append(String.format(Constants.SUCCESSFUL_IMPORT_MESSAGE,"Town",townImportDto.getName()))
                    .append(System.lineSeparator());
        }
        return importResult.toString().trim();
    }

    @Override
    public String exportRacingTowns() {
        StringBuilder exportResult = new StringBuilder();
        List<Town>towns = this.townRepository.exportTownsByRacerCount();
        for (Town town : towns) {
            exportResult.append(String.format("Name: %s",town.getName())).append(System.lineSeparator());
            exportResult.append(String.format("Racers: %d",town.getRacers().size())).append(System.lineSeparator());
            exportResult.append(System.lineSeparator());
        }
        return exportResult.toString().trim();
    }
}
