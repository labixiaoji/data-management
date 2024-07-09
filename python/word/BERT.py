import pandas as pd
from transformers import BertTokenizer, BertModel
import torch
from scipy.spatial.distance import cosine
from scipy.stats import spearmanr

# 1. 加载BERT模型和分词器
tokenizer = BertTokenizer.from_pretrained('bert-base-uncased')
model = BertModel.from_pretrained('bert-base-uncased')

# 2. 读取Excel文件
df = pd.read_excel('F:\Desktop\Studying\毕业设计\数据\SimLex-999\SimLex-999.xlsx')  # 修改为你的文件路径

# 3. 函数：计算BERT嵌入的余弦相似度
def bert_similarity(word1, word2):
    # 将单词转换为BERT可接受的格式
    inputs1 = tokenizer(word1, return_tensors="pt")
    inputs2 = tokenizer(word2, return_tensors="pt")

    # 获取嵌入
    with torch.no_grad():
        outputs1 = model(**inputs1)
        outputs2 = model(**inputs2)

    # 使用[CLS] token的嵌入，确保是一维的
    embedding1 = outputs1.last_hidden_state[:, 0, :].squeeze()  # .squeeze() 去掉单一维度
    embedding2 = outputs2.last_hidden_state[:, 0, :].squeeze()

    # 计算余弦相似度，确保embedding1和embedding2是一维的
    return 1 - cosine(embedding1.numpy(), embedding2.numpy())

# 4. 计算相似度并更新DataFrame
similarities = []
model_scores = []
human_scores = []

for index, row in df.iterrows():
    word1 = row['word1']
    word2 = row['word2']
    human_score = row['SimLex999']
    model_score = bert_similarity(word1, word2)
    model_scores.append(model_score)
    human_scores.append(human_score)
    similarities.append(model_score)

df['BERT_Similarity'] = similarities

# 5. 计算相关系数
correlation, _ = spearmanr(human_scores, model_scores)
print("模型相关系数:", correlation)

# 6. 保存新的Excel文件
df.to_excel('F:\Desktop\Studying\毕业设计\数据\SimLex-999\SimLex-999-1.xlsx', index=False)  # 修改为你想保存的路径
