import React from "react";
import { EmojiFoodBeverage } from "./EmojiFoodBeverage";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "EmojiFoodBeverage",
  component: EmojiFoodBeverage,
};

export const Default = () => <EmojiFoodBeverage />;
export const Fill = () => <EmojiFoodBeverage fill="blue" />;
export const Size = () => <EmojiFoodBeverage height="50" width="50" />;
export const CustomStyle = () => <EmojiFoodBeverage style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <EmojiFoodBeverage className="custom-class" />;

export const Clickable = () => <EmojiFoodBeverage onClick={()=>console.log("clicked")} />;

const Template = (args) => <EmojiFoodBeverage {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
