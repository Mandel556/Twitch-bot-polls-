fetch('/api/poll/status')
  .then(response => response.json())
  .then(data => {
    // Mettre à jour ton UI
    updatePollDisplay(data);
  });

  setInterval(() => {
  fetchPollData();
}, 2000);