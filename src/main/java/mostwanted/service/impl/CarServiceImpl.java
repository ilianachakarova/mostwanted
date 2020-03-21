package mostwanted.service.impl;

import com.google.gson.Gson;
import mostwanted.common.Constants;
import mostwanted.domain.dtos.CarImportDto;
import mostwanted.domain.entities.Car;
import mostwanted.domain.entities.Racer;
import mostwanted.repository.CarRepository;
import mostwanted.repository.RacerRepository;
import mostwanted.service.CarService;
import mostwanted.util.FileUtil;
import mostwanted.util.ValidationUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class CarServiceImpl implements CarService {
    private final CarRepository carRepository;
    private final FileUtil fileUtil;
    private final Gson gson;
    private final ValidationUtil validationUtil;
    private final ModelMapper modelMapper;
    private final RacerRepository racerRepository;

    private final static String CAR_JSON_FILE = "src/main/resources/files/cars.json";
    @Autowired
    public CarServiceImpl(CarRepository carRepository, FileUtil fileUtil, Gson gson, ValidationUtil validationUtil, ModelMapper modelMapper, RacerRepository racerRepository) {
        this.carRepository = carRepository;
        this.fileUtil = fileUtil;
        this.gson = gson;
        this.validationUtil = validationUtil;
        this.modelMapper = modelMapper;
        this.racerRepository = racerRepository;
    }

    @Override
    public Boolean carsAreImported() {
        return this.carRepository.count()>0;
    }

    @Override
    public String readCarsJsonFile() throws IOException {
        return this.fileUtil.readFile(CAR_JSON_FILE);
    }

    @Override
    public String importCars(String carsFileContent) {
        StringBuilder importResult = new StringBuilder();
        CarImportDto[] carImportDtos = this.gson.fromJson(carsFileContent, CarImportDto[].class);

        for (CarImportDto carImportDto : carImportDtos) {
            Car car = this.carRepository.findByBrandAndModel(carImportDto.getBrand(), carImportDto.getModel()).orElse(null);
            if(car!=null){
                importResult.append(Constants.DUPLICATE_DATA_MESSAGE).append(System.lineSeparator());
                continue;
            }
            Racer racer = this.racerRepository.findByName(carImportDto.getRacerName()).orElse(null);
            if(!this.validationUtil.isValid(carImportDto) || racer == null){
                importResult.append(Constants.INCORRECT_DATA_MESSAGE).append(System.lineSeparator());
                continue;
            }

            car = this.modelMapper.map(carImportDto, Car.class);
            car.setRacer(racer);
            this.carRepository.saveAndFlush(car);

            importResult.append(String.format(Constants.SUCCESSFUL_IMPORT_MESSAGE,"Car",String.format("%s %s @ %d",
                    car.getBrand(), car.getModel(), car.getYearOfProduction()))).append(System.lineSeparator());
        }
        return importResult.toString().trim();
    }
}
