// Function to fetch the list of all study plans and find the ID by subject and module
function fetchStudyPlan() {
    const subjectInput = document.getElementById("subjectName").value.trim().toLowerCase();
    let moduleInput = '';
    const modulePrompt = document.getElementById("moduleName");
    if (modulePrompt) {
        moduleInput = modulePrompt.value.trim().toLowerCase();
    } else {
        moduleInput = prompt("Enter Module Name (or leave blank to fetch latest):").trim().toLowerCase();
    }

    if (!subjectInput) {
        alert("⚠️ Please enter a subject name!");
        return;
    }
    if (!moduleInput) {
        alert("⚠️ Please enter a module name!");
        return;
    }

    // Use the new API endpoint to fetch by user, subject, and module
    const userId = localStorage.getItem('userId') || sessionStorage.getItem('userId') || '';
    if (!userId) {
        alert('User ID not found. Please log in again.');
        return;
    }
    fetch(`http://localhost:8082/api/studyplan/user/${userId}/subject/${encodeURIComponent(subjectInput)}/module/${encodeURIComponent(moduleInput)}`)
        .then(handleResponse)
        .then(data => {
            if (!data.studyPlan) {
                alert(`⚠️ No study plan found for \"${subjectInput}\" and module \"${moduleInput}\"!`);
                return;
            }
            displayStudyPlan(data.studyPlan);
        })
        .catch(error => {
            console.error("❌ Error fetching study plan:", error);
            alert("⚠️ Unable to fetch study plan. Please try again later.");
        });
}

// Function to fetch study plan by ID
function fetchStudyPlanById(studyPlanId) {
    fetch(`http://localhost:8082/api/studyplan/${studyPlanId}`)
        .then(handleResponse)
        .then(data => {
            if (!data.studyPlan) {
                alert("⚠️ Study Plan not found!");
                return;
            }
            displayStudyPlan(data.studyPlan);
        })
        .catch(error => {
            console.error("❌ Error fetching study plan:", error);
            alert("⚠️ Study Plan not found!");
        });
}

// Enhanced study plan display function
function displayStudyPlan(data) {
    const studyPlanContainer = document.getElementById("study-plan");
    
    const planInfo = {
        subject: (data.subject && data.subject !== 'undefined') ? data.subject : "Unknown Subject",
        hoursPerDay: (data.hoursPerDay !== undefined && data.hoursPerDay !== null && data.hoursPerDay !== 'undefined') ? data.hoursPerDay : "N/A",
        duration: (data.duration !== undefined && data.duration !== null && data.duration !== 'undefined') ? `${data.duration} days` : "N/A",
        learningStyle: (data.learningStyle && data.learningStyle !== 'undefined') ? data.learningStyle : "Not specified",
        studyData: (data.studyData && data.studyData !== 'undefined') ? data.studyData : ""
    };

    studyPlanContainer.innerHTML = `
        <h2>${planInfo.subject} Study Plan</h2>
        <div class="study-info">
            <p><strong>⏳ Hours per day:</strong> ${planInfo.hoursPerDay}</p>
            <p><strong>📆 Duration:</strong> ${planInfo.duration}</p>
            <p><strong>🎨 Learning Style:</strong> ${planInfo.learningStyle}</p>
        </div>
        <h3>📅 Study Plan Details:</h3>
        ${processStudyData(planInfo.studyData)}
        <button onclick="deleteStudyPlan(${data.id})" class="delete-btn">❌ Delete</button>
    `;

    renderSchedule(data);
}

// Process study data into structured format
function processStudyData(studyData) {
    if (!studyData || studyData === 'undefined') return "<p>No study details available</p>";
    
    const days = studyData.split(/Day \d+:/).slice(1);
    return days.map((dayContent, index) => {
        const [header, ...sections] = dayContent.split(/Key Points:|Recommended Resources:/);
        const [title, timing] = header.split(/\(\d+ hours\)/);
        
        return `
            <div class="day-container">
                <div class="day-header">
                    <h3>Day ${index + 1}: ${title.trim()}</h3>
                    <span class="duration">${timing?.trim() || ''}</span>
                </div>
                ${processKeyPoints(sections[0])}
                ${processResources(sections[1])}
            </div>
        `;
    }).join('');
}

// Process key points into list items
function processKeyPoints(content) {
    if (!content) return '';
    
    const points = content.split('\n')
        .map(p => p.trim())
        .filter(p => p.length > 0 && !p.startsWith('(') && !p.endsWith(')'));

    return `
        <div class="key-points">
            <h4>📝 Key Points:</h4>
            <ul>${points.map(p => `<li>${p.replace(/^-/, '').trim()}</li>`).join('')}</ul>
        </div>
    `;
}

// Process resources into clickable links with proper formatting
function processResources(content) {
    if (!content) return '';
    
    // Extract resources using regex to handle different formats
    const resourceRegex = /-\s*(PDF|VIDEO|ARTICLE):\s*([^(]+)\s*\(([^)]+)\)/gi;
    const resources = [];
    let match;
    
    while ((match = resourceRegex.exec(content)) !== null) {
        let link = match[3].trim();
        const title = match[2].trim();
        const type = match[1].toLowerCase();
        
        // Get topic-specific URL based on the resource title and type
        link = getTopicSpecificResource(title, type);

        resources.push({
            type: type,
            title: title,
            link: link
        });
    }

    return `
        <div class="resources">
            <h4>📚 Recommended Resources:</h4>
            ${resources.map(resource => `
                <div class="resource-item ${resource.type.toLowerCase()}-type">
                    <a href="${resource.link}" 
                       class="resource-link" 
                       target="_blank"
                       rel="noopener noreferrer"
                       onclick="return validateResourceLink(event, '${resource.link}')">
                        <span class="resource-type">${resource.type}</span>
                        ${resource.title}
                    </a>
                </div>
            `).join('')}
        </div>
    `;
}

function getTopicSpecificResource(title, type) {
    // Convert title to lowercase for easier matching
    const titleLower = title.toLowerCase();
    
    // Resource mapping based on topics with more specific matches
    const resourceMap = {
        // Introduction and Basic Concepts
        'introduction to formal language': {
            video: 'https://www.youtube.com/watch?v=HfwGjrAPrO8',
            pdf: 'https://www.tutorialspoint.com/automata_theory/automata_theory_introduction.htm',
            article: 'https://www.shaalaa.com/question-bank-solutions/formal-languages-automata-theory-introduction_597'
        },
        'formal language definition': {
            video: 'https://www.youtube.com/watch?v=HfwGjrAPrO8',
            pdf: 'https://www.geeksforgeeks.org/introduction-of-formal-languages/',
            article: 'https://www.shaalaa.com/question-bank-solutions/formal-language-definition-types_598'
        },
        'alphabets strings operations': {
            video: 'https://www.youtube.com/watch?v=ZYpj8Jdtk1Y',
            pdf: 'https://www.geeksforgeeks.org/operations-on-strings/',
            article: 'https://www.shaalaa.com/question-bank-solutions/strings-operations_599'
        },
        'regular expression': {
            video: 'https://www.youtube.com/watch?v=9PLnN3k9Iqs',
            pdf: 'https://www.geeksforgeeks.org/regular-expressions-regular-grammar-and-regular-languages/',
            article: 'https://www.shaalaa.com/question-bank-solutions/regular-expressions-languages_358'
        },
        'finite automata': {
            video: 'https://www.youtube.com/watch?v=Qa6csfkK7_I',
            pdf: 'https://www.javatpoint.com/finite-automata',
            article: 'https://www.shaalaa.com/question-bank-solutions/finite-automata_359'
        },
        'nfa': {
            video: 'https://www.youtube.com/watch?v=eqCkkC9A0Q4',
            pdf: 'https://www.geeksforgeeks.org/introduction-of-non-deterministic-finite-automata/',
            article: 'https://www.shaalaa.com/question-bank-solutions/non-deterministic-finite-automata_360'
        },
        'dfa': {
            video: 'https://www.youtube.com/watch?v=40i4PKpM0cI',
            pdf: 'https://www.geeksforgeeks.org/introduction-of-deterministic-finite-automata-dfa/',
            article: 'https://www.shaalaa.com/question-bank-solutions/deterministic-finite-automata_361'
        },
        'grammar': {
            video: 'https://www.youtube.com/watch?v=h4s5M-eZGyY',
            pdf: 'https://www.geeksforgeeks.org/types-of-grammar-chomsky-hierarchy/',
            article: 'https://www.shaalaa.com/question-bank-solutions/context-free-grammar_360'
        },
        'pushdown automata': {
            video: 'https://www.youtube.com/watch?v=FPxYePJXD6E',
            pdf: 'https://www.geeksforgeeks.org/pushdown-automata-introduction/',
            article: 'https://www.shaalaa.com/question-bank-solutions/pushdown-automata_362'
        },
        'turing machine': {
            video: 'https://www.youtube.com/watch?v=dNRDvLACg5Q',
            pdf: 'https://www.geeksforgeeks.org/turing-machine-introduction/',
            article: 'https://www.shaalaa.com/question-bank-solutions/turing-machine_363'
        }
    };

    // Find the most specific matching topic
    let matchedTopic = null;
    let maxMatchLength = 0;

    Object.keys(resourceMap).forEach(topic => {
        if (titleLower.includes(topic) && topic.length > maxMatchLength) {
            matchedTopic = topic;
            maxMatchLength = topic.length;
        }
    });

    // If we found a matching topic, return its resource
    if (matchedTopic) {
        return resourceMap[matchedTopic][type] || getDefaultResource(type, titleLower);
    }

    // Return default resource if no specific match found
    return getDefaultResource(type, titleLower);
}

function getDefaultResource(type, title) {
    // Generic resources based on type
    const defaultResources = {
        video: {
            theory: 'https://www.youtube.com/watch?v=58N2N7zJGrQ',
            practice: 'https://www.youtube.com/watch?v=9PLnN3k9Iqs',
            examples: 'https://www.youtube.com/watch?v=Qa6csfkK7_I'
        },
        pdf: {
            theory: 'https://www.tutorialspoint.com/automata_theory/automata_theory_introduction.htm',
            practice: 'https://www.geeksforgeeks.org/regular-expressions-regular-grammar-and-regular-languages/',
            examples: 'https://www.javatpoint.com/finite-automata'
        },
        article: {
            theory: 'https://www.shaalaa.com/question-bank-solutions/theory-computation_357',
            practice: 'https://www.shaalaa.com/question-bank-solutions/regular-expressions-languages_358',
            examples: 'https://www.shaalaa.com/question-bank-solutions/finite-automata_359'
        }
    };

    // Determine content type based on title
    let contentType = 'theory';
    if (title.includes('example') || title.includes('problem')) {
        contentType = 'examples';
    } else if (title.includes('practice') || title.includes('exercise')) {
        contentType = 'practice';
    }

    return defaultResources[type][contentType];
}

// Add this function to validate links before opening
function validateResourceLink(event, link) {
    if (!link.startsWith('http')) {
        alert('Invalid resource link. Please try another resource.');
        event.preventDefault();
        return false;
    }
    return true;
}

// Schedule rendering (unchanged)
function renderSchedule(data) {
    const scheduleContainer = document.getElementById("schedule");
    scheduleContainer.innerHTML = "";

    const daySection = document.createElement("div");
    daySection.classList.add("day-section");
    daySection.innerHTML = `<h2>📆 ${data.subject || "Unknown"} Schedule</h2>`;

    const item = document.createElement("div");
    item.classList.add("schedule-item");
    item.innerHTML = `
        <span class="subject">${data.subject || "Unknown"}</span>
        <p><strong>⏳ Hours per day:</strong> ${data.hoursPerDay ?? "N/A"}</p>
        <p><strong>📆 Duration:</strong> ${data.duration ? `${data.duration} days` : "N/A"}</p>
        <p><strong>🎨 Learning Style:</strong> ${data.learningStyle || "Not specified"}</p>
    `;

    daySection.appendChild(item);
    scheduleContainer.appendChild(daySection);
}

// Delete function (unchanged)
function deleteStudyPlan(studyPlanId) {
    if (!confirm("Are you sure you want to delete this study plan?")) return;

    fetch(`http://localhost:8082/api/studyplan/${studyPlanId}`, { method: "DELETE" })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => {
                    throw new Error(text || 'Failed to delete study plan');
                });
            }
            return response.json(); // Parse as JSON instead of text
        })
        .then(data => {
            alert(`✅ ${data.message}`); // Properly formatted message
            document.getElementById("study-plan").innerHTML = "";
            document.getElementById("schedule").innerHTML = "";
            document.querySelector(".input-section").classList.remove("hidden");
        })
        .catch(error => {
            console.error("❌ Error deleting study plan:", error);
            alert(`⚠️ ${error.message}`); // Proper error formatting
        });
}

// Helper function for response handling
function handleResponse(response) {
    if (!response.ok) return response.text().then(text => { throw new Error(text); });
    return response.json();
}

// Updated JavaScript with input section hiding
function fetchStudyPlan() {
    const subjectInput = document.getElementById("subjectName").value.trim().toLowerCase();
    const inputSection = document.querySelector(".input-section");

    if (!subjectInput) {
        alert("⚠️ Please enter a subject name!");
        return;
    }

    fetch(`http://localhost:8082/api/studyplan/all`)
        .then(handleResponse)
        .then(studyPlans => {
            if (!Array.isArray(studyPlans) || studyPlans.length === 0) {
                alert("⚠️ No study plans found!");
                return;
            }

            const matchedPlan = studyPlans.find(plan => 
                plan.subject && plan.subject.toLowerCase() === subjectInput
            );

            if (!matchedPlan) {
                alert(`⚠️ No study plan found for "${subjectInput}"!`);
                return;
            }

            inputSection.classList.add("hidden");
            fetchStudyPlanById(matchedPlan.id);
        })
        .catch(error => {
            console.error("❌ Error fetching study plans:", error);
            alert("⚠️ Unable to fetch study plans. Please try again later.");
        });
}