/*
 Navicat Premium Data Transfer

 Source Server         : ywx
 Source Server Type    : MySQL
 Source Server Version : 80029
 Source Host           : localhost:3306
 Source Schema         : examination

 Target Server Type    : MySQL
 Target Server Version : 80029
 File Encoding         : 65001

 Date: 22/12/2023 10:18:22
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for answers
-- ----------------------------
DROP TABLE IF EXISTS `answers`;
CREATE TABLE `answers`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `question_id` int(0) NULL DEFAULT NULL,
  `correct_answer_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `question_id`(`question_id`) USING BTREE,
  CONSTRAINT `answers_ibfk_1` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of answers
-- ----------------------------
INSERT INTO `answers` VALUES (1, 9, '\"同穴之木，心而已矣\"这句话的意思是，像同处一个洞穴的两棵树一样，它们的根相交在一起，心意已经非常合一了，表达了深厚的感情或默契。');
INSERT INTO `answers` VALUES (2, 18, '化学符号 \"Na\" 代表钠元素。');
INSERT INTO `answers` VALUES (6, 53, '霓为衣兮风为马，云之君兮纷纷而来下');
INSERT INTO `answers` VALUES (8, 63, '士不可以不弘毅，任重而道远');

-- ----------------------------
-- Table structure for login
-- ----------------------------
DROP TABLE IF EXISTS `login`;
CREATE TABLE `login`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `is_admin` tinyint(1) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of login
-- ----------------------------
INSERT INTO `login` VALUES (1, 'ywx', '123456', 1);
INSERT INTO `login` VALUES (2, 'fzy', '123456', 0);
INSERT INTO `login` VALUES (7, 'user1', '123456', 1);
INSERT INTO `login` VALUES (8, 'user2', '123456', 1);
INSERT INTO `login` VALUES (9, 'fzy2', '123456', 0);

-- ----------------------------
-- Table structure for options
-- ----------------------------
DROP TABLE IF EXISTS `options`;
CREATE TABLE `options`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `question_id` int(0) NULL DEFAULT NULL,
  `option_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `is_correct` tinyint(1) NULL DEFAULT NULL,
  `option_name` varchar(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `question_id`(`question_id`) USING BTREE,
  CONSTRAINT `options_ibfk_1` FOREIGN KEY (`question_id`) REFERENCES `questions` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 213 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of options
-- ----------------------------
INSERT INTO `options` VALUES (1, 2, '秦爱纷奢', 0, 'A');
INSERT INTO `options` VALUES (2, 2, '齐国虽褊小，我何爱一牛', 1, 'B');
INSERT INTO `options` VALUES (3, 2, '予独爱莲之出淤泥而不染', 0, 'C');
INSERT INTO `options` VALUES (4, 2, '爱而不见，搔首踯躅', 0, 'D');
INSERT INTO `options` VALUES (9, 4, '0.7', 0, 'A');
INSERT INTO `options` VALUES (10, 4, '0.2', 0, 'B');
INSERT INTO `options` VALUES (11, 4, '0.12', 1, 'C');
INSERT INTO `options` VALUES (12, 4, '0.1', 0, 'D');
INSERT INTO `options` VALUES (13, 5, '铜', 0, 'A');
INSERT INTO `options` VALUES (14, 5, '铁', 0, 'B');
INSERT INTO `options` VALUES (15, 5, '锌', 0, 'C');
INSERT INTO `options` VALUES (16, 5, '钠', 1, 'D');
INSERT INTO `options` VALUES (17, 6, '5/3', 0, 'A');
INSERT INTO `options` VALUES (18, 6, '20', 0, 'B');
INSERT INTO `options` VALUES (19, 6, '175/6', 0, 'C');
INSERT INTO `options` VALUES (20, 6, '115/3', 1, 'D');
INSERT INTO `options` VALUES (45, 10, 'the zoo', 1, 'A');
INSERT INTO `options` VALUES (46, 10, 'the village', 0, 'B');
INSERT INTO `options` VALUES (47, 10, 'the park', 0, 'C');
INSERT INTO `options` VALUES (48, 10, 'America', 0, 'D');
INSERT INTO `options` VALUES (53, 3, 'to', 1, 'A');
INSERT INTO `options` VALUES (54, 3, 'for', 0, 'B');
INSERT INTO `options` VALUES (55, 3, 'with', 0, 'C');
INSERT INTO `options` VALUES (56, 3, 'in', 0, 'D');
INSERT INTO `options` VALUES (57, 17, 'of', 0, 'A');
INSERT INTO `options` VALUES (58, 17, 'by', 0, 'B');
INSERT INTO `options` VALUES (59, 17, 'at', 0, 'C');
INSERT INTO `options` VALUES (60, 17, 'with', 1, 'D');
INSERT INTO `options` VALUES (61, 20, 'money', 0, 'A');
INSERT INTO `options` VALUES (62, 20, 'friendship', 0, 'B');
INSERT INTO `options` VALUES (63, 20, 'love', 0, 'C');
INSERT INTO `options` VALUES (64, 20, 'interest', 1, 'D');
INSERT INTO `options` VALUES (77, 25, '氧的电子构型为1s²2s²2p⁴。', 0, 'A');
INSERT INTO `options` VALUES (78, 25, '氧通常以O₂的形式存在，是一种双原子分子。', 1, 'B');
INSERT INTO `options` VALUES (79, 25, '氧的电负性较低，不容易与其他元素形成化合物。', 1, 'C');
INSERT INTO `options` VALUES (80, 25, '氧是一种具有金属性质的元素。', 0, 'D');
INSERT INTO `options` VALUES (81, 26, '氧化还原数目变化的元素是氢。', 0, 'A');
INSERT INTO `options` VALUES (82, 26, '过氧化氢是一种强氧化剂。', 1, 'B');
INSERT INTO `options` VALUES (83, 26, '它可以分解为水和氧气。', 1, 'C');
INSERT INTO `options` VALUES (84, 26, '过氧化氢是一种强还原剂。', 0, 'D');
INSERT INTO `options` VALUES (85, 27, '65', 0, 'A');
INSERT INTO `options` VALUES (86, 27, '70', 1, 'B');
INSERT INTO `options` VALUES (87, 27, '75', 0, 'C');
INSERT INTO `options` VALUES (88, 27, '80', 0, 'D');
INSERT INTO `options` VALUES (89, 28, '40', 0, 'A');
INSERT INTO `options` VALUES (90, 28, '45', 0, 'B');
INSERT INTO `options` VALUES (91, 28, '50', 1, 'C');
INSERT INTO `options` VALUES (92, 28, '60', 0, 'D');
INSERT INTO `options` VALUES (93, 29, '从六名学生中选三名学生参加数学、物理、化学竞赛，共有多少种选法', 1, 'A');
INSERT INTO `options` VALUES (94, 29, '有十二名学生参加植树活动，要求三人一组，共有多少种分组方案', 0, 'B');
INSERT INTO `options` VALUES (95, 29, '从3，5，7，9中任选两个数做指数运算，可以得到多少个幂', 1, 'C');
INSERT INTO `options` VALUES (96, 29, '从1，2，3，4中任取两个数作为点的坐标，可以得到多少个不同的点', 1, 'D');
INSERT INTO `options` VALUES (97, 30, '频率分布直方图中a的值为0.005', 1, 'A');
INSERT INTO `options` VALUES (98, 30, '估计这40名学生的竞赛成绩的第60百分位数为75', 0, 'B');
INSERT INTO `options` VALUES (99, 30, '估计这40名学生的竞赛成绩的众数为80', 0, 'C');
INSERT INTO `options` VALUES (100, 30, '估计总体中成绩落在[60，70)内的学生人数为225', 1, 'D');
INSERT INTO `options` VALUES (101, 31, '一年来，这个问题无时无刻不在缠绕着我，让我痛苦不堪；只有在非常繁忙的时候，我才会暂时忘记它。', 0, 'A');
INSERT INTO `options` VALUES (102, 31, '这座桥梁的垮塌，既不是设计方的问题，也不是施工方的，那一定是客观原因造成的。', 0, 'B');
INSERT INTO `options` VALUES (103, 31, '阅读名著不一定能让我们的语文成绩在短时间内有明显提高，但对我们语文学科素养的提升会有积淀的积极作用。', 1, 'C');
INSERT INTO `options` VALUES (104, 31, '司机质疑执法人员乱罚款，执法人员说：“罚款本身不是目的，严格执法是为了维护人民群众的合法权益。”', 0, 'D');
INSERT INTO `options` VALUES (105, 32, 'saving the lives of travellers', 1, 'A');
INSERT INTO `options` VALUES (106, 32, 'guarding the house', 0, 'B');
INSERT INTO `options` VALUES (107, 32, 'running in the dark', 0, 'C');
INSERT INTO `options` VALUES (108, 32, 'playing with people', 0, 'D');
INSERT INTO `options` VALUES (109, 33, '《论语》以语录体为主，较为集中地体现了孔子的政治主张、伦理思想、道德观念及教育原则等。', 0, 'A');
INSERT INTO `options` VALUES (110, 33, '《礼记》是儒家“十三经”中三部礼学经典之一，另外两部是记载春秋战国礼仪制度的《仪礼》和记载周王朝及各诸侯国政治规章制度的《周礼》。', 0, 'B');
INSERT INTO `options` VALUES (111, 33, '《老子》又名《南华经》，全书的思想结构是：道是德的“体”，德是道的“用”。政治上主张无为而治，权术上讲究物极必反。', 1, 'C');
INSERT INTO `options` VALUES (112, 33, '墨家学说在先秦时期影响很大，与儒家并称“显学”。墨家学说以“兼爱”为核心，《兼爱》有上中下篇，语文书中所选为上篇。 ', 0, 'D');
INSERT INTO `options` VALUES (145, 44, '3', 0, 'A');
INSERT INTO `options` VALUES (146, 44, '4', 0, 'B');
INSERT INTO `options` VALUES (147, 44, '5', 1, 'C');
INSERT INTO `options` VALUES (148, 44, '6', 0, 'D');
INSERT INTO `options` VALUES (173, 51, '2/3', 0, 'A');
INSERT INTO `options` VALUES (174, 51, '1/2', 1, 'B');
INSERT INTO `options` VALUES (175, 51, '1/3', 0, 'C');
INSERT INTO `options` VALUES (176, 51, '1/4', 0, 'D');
INSERT INTO `options` VALUES (177, 52, '2017年至2023年每年1—7月中央处理部件进口数量的中位数为583', 1, 'A');
INSERT INTO `options` VALUES (178, 52, '2017年至2023年每年1—7月中央处理部件出口数量的40%分位数为2050', 0, 'B');
INSERT INTO `options` VALUES (179, 52, '2017年至2023年每年1—7月中央处理部件出口数量的平均数超过2152', 1, 'C');
INSERT INTO `options` VALUES (180, 52, '2017年至2023年每年1—7月中央处理部件进口数量的极差小于出口数量的极差', 0, 'D');
INSERT INTO `options` VALUES (181, 54, 'perfume', 1, 'A');
INSERT INTO `options` VALUES (182, 54, 'hair gel', 0, 'B');
INSERT INTO `options` VALUES (183, 54, 'water', 0, 'C');
INSERT INTO `options` VALUES (184, 54, 'pesticide', 0, 'D');
INSERT INTO `options` VALUES (185, 55, 'by a human', 0, 'A');
INSERT INTO `options` VALUES (186, 55, 'by two towers', 1, 'B');
INSERT INTO `options` VALUES (187, 55, 'by cables', 0, 'C');
INSERT INTO `options` VALUES (188, 55, 'by rope', 0, 'D');
INSERT INTO `options` VALUES (201, 60, '推动大规模经济建设逐步展开', 0, 'A');
INSERT INTO `options` VALUES (202, 60, '体现了新民主主义经济特征', 1, 'B');
INSERT INTO `options` VALUES (203, 60, '贯彻了《论十大关系》的精神', 0, 'C');
INSERT INTO `options` VALUES (204, 60, '有利于巩固新生的人民政权', 0, 'D');
INSERT INTO `options` VALUES (205, 61, 'She won a prize.', 0, 'A');
INSERT INTO `options` VALUES (206, 61, 'It was Sunday.', 1, 'B');
INSERT INTO `options` VALUES (207, 61, 'The writer scored a hundred.', 0, 'C');
INSERT INTO `options` VALUES (208, 61, 'The Chinese team scored.', 0, 'D');
INSERT INTO `options` VALUES (209, 62, '进程I是放热反应', 0, 'A');
INSERT INTO `options` VALUES (210, 62, '平衡时P的产率：I>II', 1, 'B');
INSERT INTO `options` VALUES (211, 62, '生成P的速率：III>II', 1, 'C');
INSERT INTO `options` VALUES (212, 62, '进程IV中，Z没有催化作用', 0, 'D');

-- ----------------------------
-- Table structure for question_types
-- ----------------------------
DROP TABLE IF EXISTS `question_types`;
CREATE TABLE `question_types`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of question_types
-- ----------------------------
INSERT INTO `question_types` VALUES (1, '单项选择题');
INSERT INTO `question_types` VALUES (2, '多项选择题');
INSERT INTO `question_types` VALUES (5, '问答题');
INSERT INTO `question_types` VALUES (6, '听力题');

-- ----------------------------
-- Table structure for questions
-- ----------------------------
DROP TABLE IF EXISTS `questions`;
CREATE TABLE `questions`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `difficulty` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `topic_id` int(0) NULL DEFAULT NULL,
  `question_type_id` int(0) NULL DEFAULT NULL,
  `score` int(0) NULL DEFAULT NULL,
  `audio_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `image_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `question_type_id`(`question_type_id`) USING BTREE,
  INDEX `questions_ibfk_1`(`topic_id`) USING BTREE,
  CONSTRAINT `questions_ibfk_1` FOREIGN KEY (`topic_id`) REFERENCES `topics` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `questions_ibfk_2` FOREIGN KEY (`question_type_id`) REFERENCES `question_types` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 64 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of questions
-- ----------------------------
INSERT INTO `questions` VALUES (2, '选出与“向使三国各爱其地”的“爱”意义相同的一项是', '难', 17, 1, 5, NULL, 'D:\\project\\Examination\\image\\directory\\1701590185482_屏幕截图 2023-07-11 212552.png');
INSERT INTO `questions` VALUES (3, 'She is allergic __________ seafood, so she can\'t eat shrimp or crab.', '易', 18, 1, 3, NULL, NULL);
INSERT INTO `questions` VALUES (4, '设事件A和事件B为两个独立事件，它们分别有概率P(A)和P(B)。若P(A) = 0.4，P(B) = 0.3，则事件A和事件B的联合概率P(A ∩ B)是多少？', '易', 10, 1, 2, NULL, NULL);
INSERT INTO `questions` VALUES (5, '在以下金属中，哪一种金属具有最高的电负性？', '易', 22, 1, 3, NULL, NULL);
INSERT INTO `questions` VALUES (6, '《莱茵德纸草书》是世界上最古老的数学著作之一，书中有这样一道题目：把100个面包分给5个人，使每个人所得成等差数列，且使较大的三份之和的1/7是较小的两份之和，则最大的一份为（   ）', '易', 10, 1, 3, NULL, 'D:\\project\\Examination\\image\\directory\\1702383254958_屏幕截图 2023-12-12 201359.png');
INSERT INTO `questions` VALUES (9, '“夫人之相与也，俯而合同焉；死而不相离，同穴之木，心而已矣。夫唯桐之木、薄言巧辩而相成也。”请简要解释文中\"同穴之木，心而已矣\"这句话的意思。', '难', 17, 5, 5, NULL, NULL);
INSERT INTO `questions` VALUES (10, 'Where must the puma have come from?', '难', 25, 6, 3, 'D:\\project\\Examination\\audio\\directory\\1702382108517_1.mp3', NULL);
INSERT INTO `questions` VALUES (17, 'We were disappointed __________ the movie. It didn\'t live up to our expectations.', '易', 18, 1, 3, NULL, NULL);
INSERT INTO `questions` VALUES (18, '化学中常用符号 \"Na\" 代表哪个元素？', '易', 22, 5, 3, NULL, NULL);
INSERT INTO `questions` VALUES (20, 'What in particular does a person gain when he or she becomes a serious collector?', '易', 25, 6, 3, 'D:\\project\\Examination\\audio\\directory\\1702382370454_59.mp3', NULL);
INSERT INTO `questions` VALUES (25, '下面关于元素氧的描述，哪些是正确的？', '难', 22, 2, 5, NULL, NULL);
INSERT INTO `questions` VALUES (26, '对于过氧化氢（H2O2），以下哪些描述是正确的？', '易', 22, 2, 5, NULL, NULL);
INSERT INTO `questions` VALUES (27, '以下数据为某学校参加学科节数学竞赛决赛的10人的成绩：（单位：分）72，78，79，80，81，83，84，86，88，90．这10人成绩的第百分位数是85，则（     ）', '易', 26, 1, 3, NULL, NULL);
INSERT INTO `questions` VALUES (28, '某班的全体学生参加数学测试，成绩的频率分布直方图如图，数据的分组依次为[20, 40)，[40,60)，[60,80)，[80,100).若低于60分的人数是15，则该班的学生人数是（       ）', '易', 26, 1, 3, NULL, 'D:\\project\\Examination\\image\\directory\\1702383043511_屏幕截图 2023-12-12 200921.png');
INSERT INTO `questions` VALUES (29, '下列选项中，属于排列问题的是（   ）', '易', 27, 2, 5, NULL, NULL);
INSERT INTO `questions` VALUES (30, '某校1500名学生参加数学竞赛，随机抽取了40名学生的竞赛成绩（单位：分），成绩的频率分布直方图如图所示，则（       ）', '易', 26, 2, 5, NULL, 'D:\\project\\Examination\\image\\directory\\1702383478653_屏幕截图 2023-12-12 201745.png');
INSERT INTO `questions` VALUES (31, '下列各项中，没有逻辑错误的一项是（    ）', '难', 18, 1, 3, NULL, NULL);
INSERT INTO `questions` VALUES (32, 'What are the St. Bernard dogs used for?', '易', 25, 6, 3, 'D:\\project\\Examination\\audio\\directory\\1702384930964_8.mp3', NULL);
INSERT INTO `questions` VALUES (33, '下面有关诸子百家的文学常识，说法不正确的一项是（       ）', '易', 17, 1, 3, NULL, 'D:\\project\\Examination\\image\\directory\\1702385042437_屏幕截图 2023-12-12 204350.png');
INSERT INTO `questions` VALUES (44, '《九章算术》是我国古代数学名著，书中将底面为矩形，且有一条侧棱垂直于底面的四棱锥称为阳马.如图，在阳马P-ABCD中，PA垂直于平面ABCD，底面ABCD是正方形，PA=AB，E，F分别为PD，PB的中点，AH=aHP，CG=GP，若GH平行于平面EFC，则a=（   ）', '易', 29, 1, 3, NULL, 'D:\\project\\Examination\\image\\directory\\1702898298644_export-2023-12-18 110609.svg');
INSERT INTO `questions` VALUES (51, '某选拔性考试需要考查4个学科（语文、数学、物理、政治），则这4个学科不同的考试顺序中物理考试与数学考试不相邻的概率为（       ）', '易', 10, 1, 3, NULL, 'D:\\project\\Examination\\image\\directory\\1703072448377_五角星.png');
INSERT INTO `questions` VALUES (52, '如图为2017年至2023年每年1—7月中国中央处理部件进出口数量统计图，则下列说法正确的是（       ）', '易', 26, 2, 5, NULL, 'D:\\project\\Examination\\image\\directory\\1703073533570_屏幕截图 2023-12-20 195608.png');
INSERT INTO `questions` VALUES (53, '李白在《梦游天姥吟留别》中写仙府石门突然打开，奇异景象尽收眼底。其中表现仙人们纷纷驾风而来的两句是“_______，______”。', '中', 20, 5, 3, NULL, 'D:\\project\\Examination\\image\\directory\\1703074052294_accelerate-svgrepo-com.svg');
INSERT INTO `questions` VALUES (54, 'What was the Customs Officer looking for?', '难', 25, 6, 3, 'D:/project/Examination/audio/record/output_1703074539649.wav', NULL);
INSERT INTO `questions` VALUES (55, 'How is the bridge supported?', '中', 25, 6, 3, 'D:\\project\\Examination\\audio\\directory\\1703082095367_17.mp3', NULL);
INSERT INTO `questions` VALUES (60, '1949年4月11日，刘少奇在与民族工商业家等各界人士座谈会后，写下《天津工作问题》调查提纲：“必须正确建立与改善以下各方面的关系：即公私关系，劳资关系，城乡关系，内外关系。这四面八方的关系即全面关系都必须很好地照顾到。”这一思想（  ）', '易', 30, 1, 3, 'null', 'null');
INSERT INTO `questions` VALUES (61, 'Why was the writer\'s aunt surprised?', '中', 25, 6, 3, 'D:\\project\\Examination\\audio\\directory\\2.2.mp3', 'null');
INSERT INTO `questions` VALUES (62, '反应物(s)转化为产物(P或P·Z)能量与反应进程的关系如下图所示：下列有关四种不同反应进程的说法不正确的是', '难', 22, 2, 5, 'null', 'D:\\project\\Examination\\image\\directory\\屏幕截图 2023-12-14 211707.png');
INSERT INTO `questions` VALUES (63, '读书人要立志成为国家和社会的脊梁，承担起自己的责任，《论语·泰伯》中曾子就曾经指出：“____________，____________。”', '易', 20, 5, 6, 'null', 'null');

-- ----------------------------
-- Table structure for subjects
-- ----------------------------
DROP TABLE IF EXISTS `subjects`;
CREATE TABLE `subjects`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `subject` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of subjects
-- ----------------------------
INSERT INTO `subjects` VALUES (1, '数学');
INSERT INTO `subjects` VALUES (2, '语文');
INSERT INTO `subjects` VALUES (5, '英语');
INSERT INTO `subjects` VALUES (6, '化学');
INSERT INTO `subjects` VALUES (7, '体育');
INSERT INTO `subjects` VALUES (8, '物理');
INSERT INTO `subjects` VALUES (9, '历史');
INSERT INTO `subjects` VALUES (13, '政治');

-- ----------------------------
-- Table structure for topics
-- ----------------------------
DROP TABLE IF EXISTS `topics`;
CREATE TABLE `topics`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `subject_id` int(0) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `topics_ibfk_1`(`subject_id`) USING BTREE,
  CONSTRAINT `topics_ibfk_1` FOREIGN KEY (`subject_id`) REFERENCES `subjects` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 38 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of topics
-- ----------------------------
INSERT INTO `topics` VALUES (1, '导数', 1);
INSERT INTO `topics` VALUES (9, '积分', 1);
INSERT INTO `topics` VALUES (10, '概率', 1);
INSERT INTO `topics` VALUES (17, '文言文', 2);
INSERT INTO `topics` VALUES (18, '语法', 5);
INSERT INTO `topics` VALUES (19, '有机', 6);
INSERT INTO `topics` VALUES (20, '古诗词', 2);
INSERT INTO `topics` VALUES (21, '阅读理解', 5);
INSERT INTO `topics` VALUES (22, '无机', 6);
INSERT INTO `topics` VALUES (23, '分子', 6);
INSERT INTO `topics` VALUES (24, '篮球', 7);
INSERT INTO `topics` VALUES (25, '听力', 5);
INSERT INTO `topics` VALUES (26, '统计', 1);
INSERT INTO `topics` VALUES (27, '排列组合', 1);
INSERT INTO `topics` VALUES (28, '语法', 2);
INSERT INTO `topics` VALUES (29, '图形', 1);
INSERT INTO `topics` VALUES (30, '近现代史', 9);

SET FOREIGN_KEY_CHECKS = 1;
