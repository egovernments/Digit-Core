import React from "react";
import { Mediation } from "./Mediation";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "Mediation",
  component: Mediation,
};

export const Default = () => <Mediation />;
export const Fill = () => <Mediation fill="blue" />;
export const Size = () => <Mediation height="50" width="50" />;
export const CustomStyle = () => <Mediation style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <Mediation className="custom-class" />;

export const Clickable = () => <Mediation onClick={()=>console.log("clicked")} />;

const Template = (args) => <Mediation {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
