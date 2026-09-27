-- V7: Seed skill master data

INSERT INTO skills (name, category) VALUES
-- Programming Languages
('java', 'Programming Language'),
('python', 'Programming Language'),
('javascript', 'Programming Language'),
('typescript', 'Programming Language'),
('kotlin', 'Programming Language'),
('golang', 'Programming Language'),
('rust', 'Programming Language'),
('scala', 'Programming Language'),
('c++', 'Programming Language'),
('c#', 'Programming Language'),
('swift', 'Programming Language'),
('php', 'Programming Language'),
('ruby', 'Programming Language'),
('r', 'Programming Language'),

-- Web Frameworks
('spring boot', 'Framework'),
('spring', 'Framework'),
('django', 'Framework'),
('flask', 'Framework'),
('fastapi', 'Framework'),
('express', 'Framework'),
('nestjs', 'Framework'),
('react', 'Framework'),
('angular', 'Framework'),
('vue', 'Framework'),
('nextjs', 'Framework'),
('laravel', 'Framework'),
('rails', 'Framework'),

-- Databases
('mysql', 'Database'),
('postgresql', 'Database'),
('mongodb', 'Database'),
('redis', 'Database'),
('elasticsearch', 'Database'),
('cassandra', 'Database'),
('dynamodb', 'Database'),
('oracle', 'Database'),
('sql server', 'Database'),
('sqlite', 'Database'),

-- Cloud & DevOps
('aws', 'Cloud'),
('azure', 'Cloud'),
('gcp', 'Cloud'),
('docker', 'DevOps'),
('kubernetes', 'DevOps'),
('jenkins', 'DevOps'),
('github actions', 'DevOps'),
('terraform', 'DevOps'),
('ansible', 'DevOps'),
('linux', 'DevOps'),
('nginx', 'DevOps'),

-- Messaging & Streaming
('kafka', 'Messaging'),
('rabbitmq', 'Messaging'),
('activemq', 'Messaging'),

-- Testing
('junit', 'Testing'),
('mockito', 'Testing'),
('selenium', 'Testing'),
('cypress', 'Testing'),
('jest', 'Testing'),
('testcontainers', 'Testing'),

-- Data Science / AI
('machine learning', 'AI/ML'),
('deep learning', 'AI/ML'),
('tensorflow', 'AI/ML'),
('pytorch', 'AI/ML'),
('scikit-learn', 'AI/ML'),
('pandas', 'AI/ML'),
('numpy', 'AI/ML'),

-- Concepts
('rest api', 'Architecture'),
('graphql', 'Architecture'),
('microservices', 'Architecture'),
('ci/cd', 'Architecture'),
('agile', 'Methodology'),
('scrum', 'Methodology'),
('git', 'Tool'),
('jira', 'Tool'),
('maven', 'Tool'),
('gradle', 'Tool'),
('hibernate', 'ORM'),
('jpa', 'ORM') ON CONFLICT (name) DO NOTHING;
