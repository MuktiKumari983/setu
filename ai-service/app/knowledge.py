"""Domain knowledge used by the pure-Python AI engine.

Everything here is deterministic so the behaviour is explainable and
testable without any heavy ML runtime.
"""

# The 11 SIH-2025/26 challenge categories plus a couple of aliases.
CATEGORIES = {
    "Waste Management": [
        "waste", "garbage", "trash", "litter", "segregation", "recycl", "landfill",
        "compost", "dumping", "dumpsite", "collection", "swachh", "plastic", "e-waste",
        "bio-medical", "hazardous waste", "dustbin", "sanitation",
    ],
    "Water Quality": [
        "water", "river", "pond", "lake", "groundwater", "aquifer", "contamination",
        "pollution", "turbidity", "ph level", "eutrophication", "arsenic", "fluoride",
        "sewage", "effluent", "drinking water", "borewell", "waterborne", "cholera",
    ],
    "Public Health": [
        "health", "hospital", "clinic", "disease", "outbreak", "epidemic", "vaccination",
        "malaria", "dengue", "tuberculosis", "nutrition", "malnutrition", "maternal",
        "primary care", "telemedicine", "pharmacy", "swasthya", "diagnostic", "mental health",
    ],
    "Education": [
        "school", "education", "student", "teacher", "literacy", "digital literacy",
        "e-learning", "learning", "dropout", "curriculum", "classroom", "anglo", "tutorial",
        "padhai", "shiksha", "vocational", "skill development", "library",
    ],
    "Renewable Energy": [
        "renewable", "solar", "wind", "biogas", "biomass", "microgrid", "grid", "energy",
        "electricity", "power", "inverter", "battery", "photovoltaic", "rural electrification",
        "off-grid", "tariff", "metering", "discom",
    ],
    "Agriculture & Livelihood": [
        "farmer", "agriculture", "crop", "irrigation", "soil", "harvest", "market price",
        "mandi", "agri", "livelihood", "farming", "pesticide", "fertilizer", "yield",
        "fisherfolk", "weaver", "artisan", "dairy", "kisan",
    ],
    "Transportation": [
        "transport", "traffic", "bus", "commute", "congestion", "road", "public transit",
        "metro", "last mile", "commuter", "road safety", "accident", "parking", "rickshaw",
        "mobility", "signal", "highway",
    ],
    "Public Safety": [
        "safety", "police", "crime", "emergency", "disaster", "evacuation", "vigil",
        "surveillance", "stolen", "theft", "women safety", "child safety", "amber alert",
        "fire", "street lighting", "tamper", "patrolling", "cctv",
    ],
    "Flood & Disaster": [
        "flood", "disaster", "monsoon", "cyclone", "inundat", "early warning", "siren",
        "relief", "shelter", "river overflow", "landslide", "earthquake", "drought",
        "rescue", "dam breach", "catchment", "storm drain", "waterlogging",
    ],
    "Digital Inclusion": [
        "internet", "broadband", "connectivity", "digital divide", "last mile connectivity",
        "wifi", "cellular", "network", "data connectivity", "online services", "digital payment",
        "cyber", "digital rights", "telecom", "community network",
    ],
    "Housing & Civic Infrastructure": [
        "housing", "slum", "infrastructure", "building", "drainage", "civic", "municipal",
        "road repair", "footpath", "street", "sewage line", "pipeline", "water supply",
        "encroachment", "pothole", "streetlight",
    ],
}

# Strong signal words that push urgency up.
URGENCY_HIGH = [
    "critical", "life", "death", "deaths", "fatal", "outbreak", "epidemic", "contaminat",
    "hazard", "flooding", "flood", "collapse", "toxic", "poisoning", "immediate", "urgent",
    "emergency", "cholera", "dengue", "malaria", "crisis", "disaster", "accident",
]
URGENCY_MEDIUM = [
    "severe", "significant", "widespread", "affecting", "delay", "poor", "risk",
    "vulnerable", "health", "safety", "pollution", "shortage", "outage", "denied",
]

# Category -> typical skills for the decomposition + skill extraction.
CATEGORY_SKILLS = {
    "Waste Management": ["IoT", "Data Analytics", "GIS", "Web Development", "Computer Vision"],
    "Water Quality": ["IoT", "Sensors", "Data Analytics", "GIS", "Web Development"],
    "Public Health": ["Data Analytics", "Mobile App", "GIS", "Web Development"],
    "Education": ["Web Development", "Mobile App", "Data Analytics", "Content Design"],
    "Renewable Energy": ["IoT", "Embedded Systems", "Electrical Engineering", "Web Development"],
    "Agriculture & Livelihood": ["Data Analytics", "Mobile App", "IoT", "GIS"],
    "Transportation": ["IoT", "Data Analytics", "Algorithms", "GIS", "Web Development"],
    "Public Safety": ["IoT", "Computer Vision", "Web Development", "GIS"],
    "Flood & Disaster": ["IoT", "GIS", "Data Analytics", "Hydrological Modeling", "Web Development"],
    "Digital Inclusion": ["Web Development", "Networking", "Mobile App", "Content Design"],
    "Housing & Civic Infrastructure": ["GIS", "Civil Engineering", "Data Analytics", "Web Development"],
}

# Decomposition templates per category: 4-tuples of
# (capability, requiredSkills, description, priority)
DECOMPOSITION_TEMPLATES = {
    "Waste Management": [
        ("Field assessment & waste audit", "IoT,Data Analytics", "Baseline survey and composition analysis", "HIGH"),
        ("Smart collection & sorting", "IoT,Computer Vision", "Sensor-enabled bins and visual sorting", "HIGH"),
        ("Monitoring dashboard", "Web Development,Data Analytics", "Live route and fill-level tracking", "MEDIUM"),
        ("Community engagement", "Content Design", "Awareness and clean-up drives", "MEDIUM"),
    ],
    "Water Quality": [
        ("Sensor deployment & calibration", "IoT,Sensors", "Install water quality sensors at sample points", "HIGH"),
        ("Data pipeline & dashboard", "Data Analytics,Web Development", "Stream readings into a live dashboard", "HIGH"),
        ("Pollution source mapping", "GIS,Data Analytics", "Map contamination hotspots", "MEDIUM"),
        ("Alerting & advisory", "Mobile App,Web Development", "Push advisories to citizens", "MEDIUM"),
    ],
    "Public Health": [
        ("Disease surveillance", "Data Analytics,IoT", "Collect and analyse health data", "HIGH"),
        ("Screening & diagnostic support", "Mobile App,Web Development", "Digital screening tools", "HIGH"),
        ("Awareness & outreach", "Content Design,Mobile App", "Health literacy campaigns", "MEDIUM"),
    ],
    "Education": [
        ("Content digitisation", "Web Development,Content Design", "Create structured e-learning content", "HIGH"),
        ("Delivery & tracking platform", "Web Development,Mobile App,Data Analytics", "Platform for lessons and progress", "HIGH"),
        ("Accessibility & offline mode", "Mobile App,Web Development", "Offline-first learning", "MEDIUM"),
    ],
    "Renewable Energy": [
        ("Grid & generation monitoring", "IoT,Embedded Systems", "Telemetry for solar microgrids", "HIGH"),
        ("Demand forecasting", "Data Analytics,Algorithms", "Predict load curves", "HIGH"),
        ("Maintenance & support", "Web Development,Data Analytics", "Remote diagnostics and ticketing", "MEDIUM"),
    ],
    "Agriculture & Livelihood": [
        ("Data collection & pricing", "Mobile App,Data Analytics", "Market price and crop data pipeline", "HIGH"),
        ("Advisory engine", "Data Analytics,Mobile App", "Personalised farmer advice", "HIGH"),
        ("Market linkage", "Web Development,Mobile App", "Connect farmers to buyers", "MEDIUM"),
    ],
    "Transportation": [
        ("Data collection", "IoT,Mobile App", "GPS and commuter data", "HIGH"),
        ("Route modelling", "Data Analytics,Algorithms", "Route and schedule optimisation", "HIGH"),
        ("Commuter app", "Mobile App,Web Development", "Real-time transit information", "MEDIUM"),
    ],
    "Public Safety": [
        ("Detection system", "Computer Vision,IoT", "Video analytics for vigilance", "HIGH"),
        ("Alert & response platform", "Web Development,Mobile App", "Alert workflow for authorities", "HIGH"),
        ("Community reporting", "Mobile App,Web Development", "Crowdsourced incident reporting", "MEDIUM"),
    ],
    "Flood & Disaster": [
        ("Early warning system", "IoT,GIS", "Sensor network for flood prediction", "HIGH"),
        ("Flood modelling", "Hydrological Modeling,Data Analytics", "Inundation and zone mapping", "HIGH"),
        ("Evacuation & relief app", "Mobile App,GIS", "Sirens, alerts and shelter finder", "HIGH"),
        ("Response coordination", "Web Development,Data Analytics", "Dashboard for relief operations", "MEDIUM"),
    ],
    "Digital Inclusion": [
        ("Connectivity rollout", "Networking,Web Development", "Extend network to unserved areas", "HIGH"),
        ("Digital literacy program", "Content Design,Mobile App", "Training modules in local languages", "HIGH"),
        ("Community services hub", "Web Development,Mobile App", "E-services for villagers", "MEDIUM"),
    ],
    "Housing & Civic Infrastructure": [
        ("Infrastructure survey", "GIS,Data Analytics", "Map civic assets and gaps", "HIGH"),
        ("Drainage & water mapping", "GIS,Civil Engineering", "Model drainage and supply networks", "HIGH"),
        ("Citizen works portal", "Web Development,Data Analytics", "Report and track civic works", "MEDIUM"),
    ],
}

FALLBACK_DECOMPOSITION = [
    ("Field assessment & data collection", "Data Analytics,Mobile App", "Collect baseline field data", "HIGH"),
    ("Monitoring application", "Web Development,Data Analytics", "Build a monitoring dashboard", "MEDIUM"),
    ("Community engagement", "Content Design", "Inform and involve the community", "MEDIUM"),
]

# Skill synonyms used to detect present skills from the problem text.
SKILL_SYNONYMS = {
    "IoT": ["iot", "sensor", "arduino", "esp32", "raspberry"],
    "Embedded Systems": ["embedded", "firmware", "microcontroller"],
    "Computer Vision": ["vision", "image processing", "camera", "detection"],
    "GIS": ["gis", "mapping", "geospatial", "satellite", "location"],
    "Data Analytics": ["data", "analytics", "dashboard", "insight"],
    "Web Development": ["web", "portal", "website", "frontend", "backend"],
    "Mobile App": ["app", "mobile", "android", "application"],
    "Networking": ["network", "connectivity", "broadband", "wifi"],
    "Hydrological Modeling": ["hydrolog", "flood model"],
    "Electrical Engineering": ["electrical", "power", "grid"],
    "Civil Engineering": ["civil", "drainage", "infrastructure", "construction"],
    "Content Design": ["content", "awareness", "training"],
    "Algorithms": ["algorithm", "route", "optimiz", "predict"],
}

GENERAL_CATEGORY = "General / Cross-Cutting"

STOPWORDS = {
    "the", "a", "an", "and", "or", "but", "for", "with", "on", "in", "at", "of",
    "to", "is", "are", "was", "were", "be", "been", "being", "it", "this", "that",
    "these", "those", "we", "they", "has", "have", "had", "will", "would", "can",
    "could", "should", "may", "might", "as", "by", "from", "into", "over", "their",
    "our", "your", "its", "there", "then", "than", "so", "if", "due", "very", "many",
    "such", "also", "within", "across", "around", "about", "all", "any", "some",
}