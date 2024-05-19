import matplotlib.pyplot as plt
import numpy as np
from scipy.interpolate import interp1d

def read_file_to_list(file_path):
    with open(file_path, 'r', encoding='utf-8') as file:
        lines = file.readlines()
    # 각 줄의 끝에 있는 개행 문자를 제거합니다.
    lines = [line.strip() for line in lines]
    return lines

# 파일 경로
file_path = './dist.txt'

# 파일을 읽어 리스트로 변환
sorted_distances = read_file_to_list(file_path)


def plot_sorted_distances(sorted_distances):
    x = np.arange(len(sorted_distances))
    y = np.array(sorted_distances)

    # 1D 선형 보간
    f = interp1d(x, y, kind='cubic')

    # 더 많은 x 값으로 보간하여 부드러운 곡선 생성
    x_new = np.linspace(0, len(sorted_distances) - 1, num=500)
    y_new = f(x_new)

    plt.figure(figsize=(10, 6))
    plt.plot(x_new, y_new, label='Interpolated Curve', color='b')
    plt.title('Sorted Distances')
    plt.xlabel('Index')
    plt.ylabel('Distance')
    plt.legend()
    plt.grid(True)
    plt.show()

plot_sorted_distances(sorted_distances)