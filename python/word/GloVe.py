import pandas as pd
import numpy as np
from scipy.spatial.distance import cosine
from scipy.stats import spearmanr

# 1. 加载GloVe词向量
def load_glove_model(glove_file_path):
    """从文件加载GloVe模型"""
    glove_model = {}
    with open(glove_file_path, 'r', encoding='utf-8') as file:
        for line in file:
            parts = line.split()
            word = parts[0]
            try:
                embedding = np.array(parts[1:], dtype=np.float32)
                glove_model[word] = embedding
            except ValueError:
                print("Skipping line with invalid data:", line)
                continue
    return glove_model


glove_path = "F:\Desktop\Studying\毕业设计\Code\model\glove.840B.300d.txt"  # 修改为GloVe文件的实际路径
glove_model = load_glove_model(glove_path)

# 2. 读取Excel文件
df = pd.read_excel('F:\Desktop\Studying\毕业设计\数据\SimLex-999\SimLex-999.xlsx')

# 3. 计算单词对的相似度
def calculate_glove_similarity(model, word1, word2):
    if word1 in model and word2 in model:
        return 1 - cosine(model[word1], model[word2])
    return None  # 如果词不在模型中，返回None

similarities = [calculate_glove_similarity(glove_model, row['word1'], row['word2']) for index, row in df.iterrows()]

# 4. 添加新列
df['GloVe_Similarity'] = similarities
correlation, _ = spearmanr(df['SimLex999'].dropna(), df['GloVe_Similarity'].dropna())
print("模型相关系数", correlation)

# 5. 保存新的Excel文件
df.to_excel('F:\Desktop\Studying\毕业设计\数据\SimLex-999\SimLex-999-GloVe.xlsx', index=False)
