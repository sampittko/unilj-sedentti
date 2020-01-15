import pandas as pd
import matplotlib.pyplot as plt
import os.path
from os import path
# import seaborn as sb

def get_path(rootPath, i):
    return rootPath + str(i) + ".csv"

def get_data(rootPath):
    frames = []
    i = 1
    while path.exists(get_path(rootPath, i)):
        dataFrame = pd.read_csv(get_path(rootPath, i))
        frames.append(dataFrame)
        i += 1
    return frames

frames = get_data("../Phase2/")
print(frames)