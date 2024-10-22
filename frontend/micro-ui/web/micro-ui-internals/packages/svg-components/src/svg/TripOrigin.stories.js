import React from "react";
import { TripOrigin } from "./TripOrigin";

export default {
  tags: ['autodocs'],
  argTypes: {
    className: {
        options: ['custom-class'],
        control: { type: 'check' },
    }
  },
  title: "TripOrigin",
  component: TripOrigin,
};

export const Default = () => <TripOrigin />;
export const Fill = () => <TripOrigin fill="blue" />;
export const Size = () => <TripOrigin height="50" width="50" />;
export const CustomStyle = () => <TripOrigin style={{ border: "1px solid red" }} />;
export const CustomClassName = () => <TripOrigin className="custom-class" />;
export const Clickable = () => <TripOrigin onClick={()=>console.log("clicked")} />;

const Template = (args) => <TripOrigin {...args}/>;

export const Playground = Template.bind({});
Playground.args = {
  className: 'custom-class',
  style:{ border: "3px solid green" }
};
