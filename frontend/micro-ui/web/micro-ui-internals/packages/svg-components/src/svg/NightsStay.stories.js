import React from "react";
import { NightsStay } from "./NightsStay";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "NightsStay",
  component: NightsStay,
};

export const Default = () => <NightsStay />;
export const Fill = () => <NightsStay fill="blue" />;
export const Size = () => <NightsStay height="50" width="50" />;
export const CustomStyle = () => <NightsStay style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <NightsStay className="custom-class" />;

export const Clickable = () => <NightsStay onClick={()=>console.log("clicked")} />;

const Template = (args) => <NightsStay {...args} />;

export const Playground = Template.bind({});
Playground.args = {
  className: "custom-class",
  style: { border: "3px solid green" }
};
