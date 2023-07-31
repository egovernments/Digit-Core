import React from "react";
import { VolunteerActivism } from "./VolunteerActivism";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "VolunteerActivism",
  component: VolunteerActivism,
};

export const Default = () => <VolunteerActivism />;
export const Fill = () => <VolunteerActivism fill="blue" />;
export const Size = () => <VolunteerActivism height="50" width="50" />;
export const CustomStyle = () => <VolunteerActivism style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <VolunteerActivism className="custom-class" />;
export const Clickable = () => <VolunteerActivism onClick={()=>console.log("clicked")} />;

const Template = (args) => <VolunteerActivism {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
