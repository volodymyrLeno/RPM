# Transformation discoverer

This is a command line tool to discover data transformations from UI logs. It works with UI logs recorded by RPA UI Logger tool available at https://github.com/apromore/RPA_UILogger. In addition to this distribution, you have to download data transformation tool called Foofah (avaialble at https://github.com/umich-dbgroup/foofah) and place it inside /RPM/out/artifacts/RPM_jar folder. 

## Requirements

* Linux
* Python 2.7
* setuptools
```
$ python -m pip install U pip setuptools
```

## Usage

The executable jar file is available under RPM/out/artifacts/RPM_jar folder. It requires the following input parameters:

* logPath - path to UI log to be processed
* foofahPath - path to foofah.py file (e.g. "/home/vleno/Desktop/RPM/out/artifacts/RPM_jar/foofah-master/")
* readActions - list of read actions separated by comma (e.g. "copyCell")
* writeActions - list of write actions separated by comma (e.g. "editField,editCell")
* preprocessing - boolean flag that indicates whether segmentation and semantic filtering is required
* approach - selector of approach to be applied. You can choose between "-1" (baseline), "-2" (grouping by target), and "-3" (grouping by target and input structure)

The example of how to run the program:

```
java -jar logs/useCase_filtered.csv foofah-master/ copyCell editField,editCell false -3
```
