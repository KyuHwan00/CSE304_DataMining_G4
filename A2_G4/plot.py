import pandas as pd
import matplotlib.pyplot as plt

# CSV 파일 읽기
df = pd.read_csv('./artd-31.csv', header=None, names=['id', 'x', 'y', 'cluster'])

# 클러스터 별로 색상을 다르게 설정
clusters = df['cluster'].unique()
print(clusters)
colors = plt.get_cmap('tab10', len(clusters))

# 그래프 그리기
plt.figure(figsize=(10, 8))
for i, cluster in enumerate(clusters):
    cluster_data = df[df['cluster'] == cluster]
    plt.scatter(cluster_data['x'], cluster_data['y'], color=colors(i), label=f'Cluster {cluster}', alpha=0.6)

plt.title('Scatter plot of clusters')
plt.xlabel('X values')
plt.ylabel('Y values')
plt.legend()
plt.show()
