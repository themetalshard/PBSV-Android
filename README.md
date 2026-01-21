PBSV was a viewer for the subgroup schedule of the Roblox group Pinewood Builders that was in development.

It relied on the website https://pinewoodbuilders.org to get the data for the schedules.
On 21/01/2026 because of additional Cloudflare security the app could no longer fetch data from the website causing it to break.

I've open sourced it here, and if you want to try it out, host an HTML file with the following JSON data as an example and modify the ScheduleRepository.kt file.
<script id="schedule-data" type="application/json">{"pbst":{"845e99ce-1862-43e2-8808-ec3dcccbce56":{"Duration":5334,"EventType":"10x Coolant Pipe Meltdown Raid","Time":2459174280,"Notes":"See you there!","Trainer":"Hurrah123456","TrainerId":135352631,"TrainingID":"845e99ce-1862-43e2-8808-ec3dcccbce56","TrainerCommsId":"331466244131782661","EventColor":[218,165,32]}</script> 

# PLEASE NOTE:
Some of the code may not be optimized or perfect as I am pretty new to Android developing + there may be unused functionality from older versions.
